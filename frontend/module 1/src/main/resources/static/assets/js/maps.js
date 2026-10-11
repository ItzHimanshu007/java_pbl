'use strict';
(() => {
  if (typeof L === 'undefined') return;
  const civic = window.civic, t = civic.t, context = civic.context, JAIPUR = [26.9124, 75.7873];
  const usable = p => p && Number.isFinite(Number(p.latitude)) && Number.isFinite(Number(p.longitude))
    && Math.abs(Number(p.latitude)) <= 90 && Math.abs(Number(p.longitude)) <= 180;
  const safeCode = code => /^[a-z-]{1,30}$/.test(code || '') ? code : 'other';
  const remember = p => { try { sessionStorage.setItem('civic_location', JSON.stringify(p)); } catch { /* storage unavailable */ } };
  const recalled = () => { try { return JSON.parse(sessionStorage.getItem('civic_location')); } catch { return null; } };
  const isDark = () => {
    const theme = document.documentElement.dataset.theme;
    return theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);
  };
  const gps = () => new Promise((resolve, reject) => {
    if (!navigator.geolocation) { reject(new Error('Location is unavailable. Search an address or choose a map pin.')); return; }
    navigator.geolocation.getCurrentPosition(
      p => resolve({ latitude: p.coords.latitude, longitude: p.coords.longitude, accuracy: p.coords.accuracy }),
      () => reject(new Error('Location could not be accessed. Search an address or choose a map pin.')),
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 });
  });
  const get = async path => {
    const response = await fetch(context + path, { credentials: 'same-origin', headers: { Accept: 'application/json' } });
    if (!response.ok) throw new Error('Address search is unavailable. Choose a map pin and enter a landmark.');
    return response.json();
  };

  // ---- Base map with layer switcher, fullscreen and locate controls ----
  const BASEMAPS = {
    streets: () => L.tileLayer(isDark()
      ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
      : 'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',
    { subdomains: 'abcd', maxZoom: 20, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>' }),
    satellite: () => L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
      { maxZoom: 19, attribution: 'Imagery &copy; Esri, Maxar, Earthstar Geographics' }),
    osm: () => L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      { maxZoom: 19, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors' })
  };
  const LAYER_LABELS = { streets: 'Streets', satellite: 'Satellite', osm: 'OSM' };

  function message(container, key) {
    let box = container.querySelector('.map-message');
    if (!box) { box = document.createElement('div'); box.className = 'map-message'; box.setAttribute('role', 'status'); container.append(box); }
    box.textContent = t(key);
  }

  function baseMap(element, { center = JAIPUR, zoom = 13, layers = true, fullscreen = true, interactive = true } = {}) {
    const map = L.map(element, { scrollWheelZoom: false, zoomControl: false, dragging: interactive, tap: interactive }).setView(center, zoom);
    L.control.zoom({ position: 'bottomright', zoomInTitle: t('Zoom in'), zoomOutTitle: t('Zoom out') }).addTo(map);
    let current = null, failures = 0, name = 'streets';
    const use = key => {
      if (current) map.removeLayer(current);
      name = key; failures = 0;
      current = BASEMAPS[key]().addTo(map);
      current.on('tileerror', () => {
        // Fall back to the standard OpenStreetMap tiles when a provider is unreachable.
        if (++failures === 6) {
          if (name !== 'osm') { use('osm'); markPressed(); } else message(element, 'Basemap unavailable. The report list and issue pins are still available.');
        }
      });
    };
    const switcher = L.control({ position: 'topright' });
    let markPressed = () => {};
    switcher.onAdd = () => {
      const box = L.DomUtil.create('div', 'map-controls');
      L.DomEvent.disableClickPropagation(box);
      if (layers) {
        const group = L.DomUtil.create('div', 'layer-switch', box);
        group.setAttribute('role', 'group');
        group.setAttribute('aria-label', t('Map style'));
        Object.keys(LAYER_LABELS).forEach(key => {
          const b = L.DomUtil.create('button', '', group);
          b.type = 'button'; b.textContent = t(LAYER_LABELS[key]); b.dataset.layer = key;
          b.addEventListener('click', () => { use(key); markPressed(); });
        });
        markPressed = () => group.querySelectorAll('button').forEach(b => b.setAttribute('aria-pressed', String(b.dataset.layer === name)));
      }
      if (fullscreen) {
        const full = L.DomUtil.create('button', 'map-btn', box);
        full.type = 'button'; full.title = t('Full screen map'); full.setAttribute('aria-label', t('Full screen map'));
        full.append(civic.icon('maximize'));
        full.addEventListener('click', () => {
          const on = !element.classList.contains('is-fullscreen');
          element.classList.toggle('is-fullscreen', on);
          full.replaceChildren(civic.icon(on ? 'minimize' : 'maximize'));
          setTimeout(() => map.invalidateSize(), 50);
        });
        document.addEventListener('keydown', e => {
          if (e.key === 'Escape' && element.classList.contains('is-fullscreen')) full.click();
        });
      }
      return box;
    };
    use('streets');
    switcher.addTo(map);
    markPressed();
    return map;
  }

  const pinIcon = (code, tone, count) => {
    const extra = count > 1 ? `<span class="pin-count">${Number(count)}</span>` : '';
    return L.divIcon({
      className: 'cp-pin ' + tone,
      html: `<span class="pin-body"></span><svg class="icon pin-icon" aria-hidden="true"><use href="${civic.icons}#cat-${safeCode(code)}"></use></svg>${extra}`,
      iconSize: [38, 44], iconAnchor: [19, 39], popupAnchor: [0, -36]
    });
  };
  const selectedIcon = () => L.divIcon({
    className: 'cp-pin selected-pin brand',
    html: `<span class="pin-body"></span><svg class="icon pin-icon" aria-hidden="true"><use href="${civic.icons}#map-pin"></use></svg>`,
    iconSize: [38, 44], iconAnchor: [19, 39]
  });

  // ---- Small read-only map on report pages ----
  document.querySelectorAll('[data-mini-map]').forEach(element => {
    const p = { latitude: element.dataset.latitude, longitude: element.dataset.longitude };
    if (!usable(p)) return;
    const point = [Number(p.latitude), Number(p.longitude)];
    const map = baseMap(element, { center: point, zoom: 16, layers: true, fullscreen: true });
    const tone = element.dataset.status === 'CLOSED' ? 'closed' : (element.dataset.priority || 'brand');
    L.marker(point, { icon: pinIcon(element.dataset.code, tone, 0), keyboard: false }).addTo(map);
  });

  // ---- Address / locality / pin picker (report form, profile, area search) ----
  document.querySelectorAll('[data-address-widget]').forEach(widget => {
    const lat = document.getElementById(widget.dataset.latInput), lon = document.getElementById(widget.dataset.lonInput);
    const address = widget.querySelector('[data-address]'), note = widget.querySelector('[data-map-message]') || widget.closest('.card')?.querySelector('[data-map-message]');
    const list = widget.querySelector('[role=listbox]'), picker = widget.querySelector('[data-location-picker]'), locality = widget.querySelector('[data-locality-select]');
    const isArea = widget.hasAttribute('data-area-widget'), isReport = widget.hasAttribute('data-report-location');
    if (!lat || !lon || !address || !list) return;
    let map, marker, accuracyCircle, timer, sequence = 0, reverseSequence = 0, nearSequence = 0, controller, pointSource = 'manual', locating = false;
    const point = () => lat.value !== '' && lon.value !== '' && usable({ latitude: lat.value, longitude: lon.value })
      ? { latitude: Number(lat.value), longitude: Number(lon.value) } : null;
    const status = key => { if (note) note.textContent = t(key); };
    const changed = () => document.dispatchEvent(new CustomEvent('civic:location', { detail: { point: point(), address: address.value } }));
    const closeList = () => { list.hidden = true; address.setAttribute('aria-expanded', 'false'); };
    const resetLink = () => { const related = document.getElementById('relatedReportId'); if (isReport && related) related.value = ''; };
    const category = () => document.querySelector('input[name=categoryId]:checked')?.value;

    const nearby = async () => {
      const panel = document.querySelector('[data-nearby]'), p = point();
      if (!isReport || !panel) return;
      const seq = ++nearSequence;
      if (!p || !category()) { panel.replaceChildren(); return; }
      panel.textContent = t('Checking for similar reports nearby…');
      try {
        const data = await get('/community/nearby?' + new URLSearchParams({ categoryId: category(), latitude: p.latitude, longitude: p.longitude }));
        if (seq !== nearSequence) return;
        panel.replaceChildren();
        if (!data.length) return;
        const h = document.createElement('h3'); h.textContent = t('Could this be the same issue?'); panel.append(h);
        const help = document.createElement('p'); help.className = 'help'; help.textContent = t('Link only if you are reporting the exact same issue. Each citizen counts once.'); panel.append(help);
        for (const r of data) {
          const row = document.createElement('div'); row.className = 'nearby-match';
          const a = document.createElement('a'); a.href = context + '/community/' + r.id; a.textContent = r.title + ' · ' + Math.round(r.distanceMeters) + ' m'; row.append(a);
          if (r.distanceMeters <= 150) {
            const b = document.createElement('button'); b.type = 'button'; b.className = 'btn btn-outline btn-sm'; b.textContent = t('Same issue');
            b.addEventListener('click', () => {
              document.getElementById('relatedReportId').value = String(r.id);
              panel.querySelectorAll('button').forEach(x => { x.textContent = t('Same issue'); });
              b.textContent = t('Linked to this issue');
            });
            row.append(b);
          }
          panel.append(row);
        }
      } catch { if (seq === nearSequence) panel.textContent = t('Nearby suggestions are unavailable. You can still submit the report.'); }
    };

    const setPoint = (p, center = true) => {
      if (!usable(p)) return;
      lat.value = Number(p.latitude).toFixed(6); lon.value = Number(p.longitude).toFixed(6);
      if (map) {
        const location = [Number(lat.value), Number(lon.value)];
        if (marker) marker.setLatLng(location);
        else marker = L.marker(location, { draggable: true, icon: selectedIcon(), title: t('Drag to adjust the location') }).addTo(map).on('dragend', () => {
          const ll = marker.getLatLng(); pointSource = 'manual'; resetLink();
          setPoint({ latitude: ll.lat, longitude: ll.lng }, false); reverse(point());
        });
        if (accuracyCircle) map.removeLayer(accuracyCircle);
        if (p.accuracy) accuracyCircle = L.circle(location, { radius: Math.min(2000, p.accuracy), color: '#2563eb', fillOpacity: .08, weight: 1 }).addTo(map);
        if (center) map.setView(location, 16);
      }
      nearby(); changed();
    };
    const reverse = async p => {
      const seq = ++reverseSequence;
      if (!p) return;
      try {
        const places = await get('/locations/reverse?' + new URLSearchParams({ latitude: p.latitude, longitude: p.longitude, lang: civic.lang }));
        if (seq !== reverseSequence) return;
        if (places.length) { address.value = places[0].label; changed(); } else status('Pin selected. Add a nearby landmark.');
      } catch { if (seq === reverseSequence) status('Pin selected. Address lookup is unavailable; add a landmark.'); }
    };

    if (picker) {
      map = baseMap(picker, { zoom: 12 });
      map.on('click', e => {
        pointSource = 'manual'; resetLink();
        setPoint({ latitude: e.latlng.lat, longitude: e.latlng.lng }, false);
        status('Location selected. Move the pin if needed.'); reverse(point());
      });
      if (point()) setPoint(point());
    }

    locality?.addEventListener('change', () => {
      const option = locality.selectedOptions[0];
      if (!option || !option.value) return;
      const [la, lo] = option.value.split(',').map(Number);
      address.value = option.dataset.label || option.textContent;
      pointSource = 'address'; resetLink();
      setPoint({ latitude: la, longitude: lo });
      status('Locality selected. Drag the pin to the exact spot.');
    });

    address.addEventListener('input', () => {
      clearTimeout(timer); controller?.abort();
      const seq = ++sequence; ++reverseSequence; closeList(); resetLink();
      if (pointSource === 'address') { lat.value = ''; lon.value = ''; if (marker) { map.removeLayer(marker); marker = null; } changed(); }
      const q = address.value.trim();
      if (q.length < 2) return;
      timer = setTimeout(async () => {
        controller = new AbortController();
        const bias = recalled(), params = new URLSearchParams({ q, lang: civic.lang });
        if (usable(bias)) { params.set('latitude', bias.latitude); params.set('longitude', bias.longitude); }
        try {
          const response = await fetch(context + '/locations/search?' + params, { credentials: 'same-origin', signal: controller.signal, headers: { Accept: 'application/json' } });
          if (!response.ok) throw new Error();
          const data = await response.json();
          if (seq !== sequence) return;
          list.replaceChildren();
          if (!data.length) { status('No address matches found. Try a locality name or choose a map pin.'); return; }
          data.forEach(place => {
            const b = document.createElement('button'); b.type = 'button'; b.setAttribute('role', 'option');
            b.append(civic.icon('map-pin'), document.createTextNode(place.label));
            b.addEventListener('click', () => {
              address.value = place.label; closeList(); pointSource = 'address';
              setPoint(place); status('Address selected. Check the pin on the map.');
              if (isArea) widget.requestSubmit();
            });
            list.append(b);
          });
          list.hidden = false; address.setAttribute('aria-expanded', 'true');
          status('Choose an address from the suggestions.');
        } catch (ex) {
          if (ex.name !== 'AbortError' && seq === sequence) status('Address search is unavailable. Choose a map pin and enter a landmark.');
        }
      }, 450);
    });
    address.addEventListener('keydown', e => {
      if (e.key === 'ArrowDown' && !list.hidden) { e.preventDefault(); list.querySelector('button')?.focus(); }
      if (e.key === 'Escape') closeList();
    });
    list.addEventListener('keydown', e => {
      const options = Array.from(list.querySelectorAll('button')), i = options.indexOf(document.activeElement);
      if (e.key === 'ArrowDown' || e.key === 'ArrowUp') { e.preventDefault(); options[(i + (e.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length]?.focus(); }
      if (e.key === 'Escape') { closeList(); address.focus(); }
    });
    document.addEventListener('click', e => { if (!widget.contains(e.target)) closeList(); });

    const locate = async () => {
      if (locating) return;
      locating = true; status('Requesting your location…');
      const btn = widget.querySelector('[data-locate]'), submit = isReport ? widget.closest('form').querySelector('button[type=submit]') : null;
      if (btn) btn.disabled = true; if (submit) submit.disabled = true;
      try {
        const p = await gps(); remember(p); pointSource = 'manual'; resetLink();
        setPoint(p); status('Location selected. Move the pin if needed.'); await reverse(p);
        if (isArea) widget.requestSubmit();
      } catch (ex) { status(ex.message); }
      finally { locating = false; if (btn) btn.disabled = false; if (submit) submit.disabled = false; }
    };
    widget.querySelector('[data-locate]')?.addEventListener('click', locate);
    widget.querySelector('[data-clear-location]')?.addEventListener('click', () => {
      lat.value = ''; lon.value = ''; address.value = ''; ++reverseSequence; resetLink();
      if (marker) { map.removeLayer(marker); marker = null; }
      status('Home area cleared.'); changed();
    });
    if (isReport) {
      document.addEventListener('civic:photo-captured', () => {
        lat.value = ''; lon.value = ''; address.value = ''; resetLink(); ++reverseSequence;
        if (marker) { map.removeLayer(marker); marker = null; }
        locate();
      });
      document.querySelectorAll('input[name=categoryId]').forEach(r => r.addEventListener('change', () => { resetLink(); nearby(); }));
      const form = widget.closest('form');
      form.addEventListener('submit', e => {
        if (locating) { e.preventDefault(); status('Wait for the location request to finish.'); return; }
        if (!point()) { e.preventDefault(); status('Select a locality, an address suggestion or a map pin before submitting.'); address.focus(); }
      });
      // Reuse an explicitly granted location. A new request still obtains fresh coordinates.
      if (!point() && navigator.permissions) navigator.permissions.query({ name: 'geolocation' }).then(p => { if (p.state === 'granted') locate(); }).catch(() => {});
    }
    if (isArea) {
      widget.addEventListener('submit', () => { if (!point()) { lat.disabled = true; lon.disabled = true; } });
      if (widget.dataset.hasArea !== 'true' && usable(recalled())) { setPoint(recalled(), false); widget.requestSubmit(); }
    }
  });

  const welcome = document.querySelector('[data-welcome-location]');
  if (welcome) welcome.addEventListener('click', async () => {
    const note = document.querySelector('[data-welcome-message]');
    welcome.disabled = true;
    try { remember(await gps()); note.textContent = t('Location enabled for this session. You can change it when reporting.'); }
    catch (ex) { note.textContent = t(ex.message); }
    finally { welcome.disabled = false; }
  });

  // ---- Community map: clustered pins synced with the issue cards ----
  const community = document.querySelector('[data-community-map]');
  if (community) {
    const map = baseMap(community, { zoom: 12 });
    const clustered = typeof L.markerClusterGroup === 'function';
    const group = clustered ? L.markerClusterGroup({
      showCoverageOnHover: false, maxClusterRadius: 42, spiderfyOnMaxZoom: true,
      iconCreateFunction: cluster => {
        const high = cluster.getAllChildMarkers().some(m => m.options.priority === 'high');
        return L.divIcon({ html: `<span>${cluster.getChildCount()}</span>`, className: 'cp-cluster' + (high ? ' has-high' : ''), iconSize: [44, 44] });
      }
    }) : L.layerGroup();
    group.addTo(map);
    const entries = [];
    const focusCard = card => {
      document.querySelectorAll('.feed-card.is-focused').forEach(el => el.classList.remove('is-focused'));
      card.classList.add('is-focused');
    };
    document.querySelectorAll('[data-community-report]').forEach(card => {
      const d = card.dataset, p = [Number(d.latitude), Number(d.longitude)];
      if (!usable({ latitude: p[0], longitude: p[1] })) return;
      const tone = d.status === 'CLOSED' ? 'closed' : d.priority;
      const marker = L.marker(p, { icon: pinIcon(d.code, tone, Number(d.count)), priority: d.priority, title: d.title, riseOnHover: true });
      const box = document.createElement('div'), title = document.createElement('strong'), chips = document.createElement('div'), meta = document.createElement('p'), link = document.createElement('a');
      box.className = 'map-popup'; chips.className = 'chips';
      title.textContent = d.title;
      const statusChip = document.createElement('span'); statusChip.className = 'chip status status-' + d.status; statusChip.textContent = t(d.status);
      const priorityChip = document.createElement('span'); priorityChip.className = 'chip priority-' + d.priority; priorityChip.textContent = t(d.priority) + ' ' + t('priority');
      chips.append(statusChip, priorityChip);
      meta.textContent = d.category + ' · ' + d.reference;
      link.href = context + '/community/' + d.id; link.textContent = t('View progress') + ' →';
      box.append(title, chips, meta, link);
      marker.bindPopup(box);
      marker.on('click', () => { focusCard(card); card.scrollIntoView({ behavior: 'smooth', block: 'nearest' }); });
      card.addEventListener('click', e => {
        if (e.target.closest('a,button,form')) return;
        focusCard(card);
        const open = () => { map.setView(p, Math.max(map.getZoom(), 16)); marker.openPopup(); };
        if (clustered) group.zoomToShowLayer(marker, open); else open();
      });
      group.addLayer(marker);
      entries.push({ card, marker, p });
    });
    const lat = community.dataset.latitude, lon = community.dataset.longitude;
    if (lat !== '' && lon !== '' && usable({ latitude: lat, longitude: lon })) {
      const center = [Number(lat), Number(lon)];
      const ring = L.circle(center, { radius: Number(community.dataset.radius) * 1000, color: '#c2416b', weight: 1.5, dashArray: '6 6', fillOpacity: .04 }).addTo(map);
      L.marker(center, { icon: L.divIcon({ className: 'home-dot', iconSize: [16, 16] }), keyboard: false }).addTo(map).bindTooltip(t('Selected area'));
      map.fitBounds(ring.getBounds(), { padding: [20, 20] });
    } else if (entries.length) map.fitBounds(entries.map(e => e.p), { padding: [40, 40], maxZoom: 15 });
    // The list shows a page of cards at a time; the map always shows every matching pin.
    const PAGE = 12, more = document.querySelector('[data-show-more]'), filter = document.querySelector('[data-priority-filter]');
    let limit = PAGE;
    const render = () => {
      const wanted = filter ? filter.value : 'all';
      let shown = 0, matching = 0;
      entries.forEach(({ card, marker }) => {
        const match = wanted === 'all' || card.dataset.priority === wanted;
        if (match) matching++;
        card.hidden = !match || shown >= limit;
        if (!card.hidden) shown++;
        if (match) group.addLayer(marker); else group.removeLayer(marker);
      });
      if (more) {
        more.hidden = matching <= shown;
        more.querySelector('span').textContent = t('Show more') + ' (' + (matching - shown) + ')';
      }
    };
    more?.addEventListener('click', () => { limit += PAGE; render(); });
    filter?.addEventListener('change', () => { limit = PAGE; render(); });
    render();
  }
})();
