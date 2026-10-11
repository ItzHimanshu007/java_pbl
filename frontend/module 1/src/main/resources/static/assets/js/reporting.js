'use strict';
(() => {
  const form = document.querySelector('[data-report-form]');
  if (!form) return;
  const t = window.civic.t;
  // Common complaint titles per service, offered as one-tap suggestions.
  const SUGGESTIONS = {
    road: ['Pothole on the road', 'Road surface badly damaged', 'Manhole cover missing', 'Speed breaker damaged'],
    streetlight: ['Streetlight not working', 'Streetlight on during the day', 'Electric pole leaning', 'Exposed street wiring'],
    garbage: ['Garbage not collected', 'Overflowing garbage bin', 'Garbage dumped on the roadside', 'Garbage being burnt'],
    water: ['No water supply', 'Low water pressure', 'Dirty water supply', 'Water pipeline leaking'],
    drain: ['Drain blocked', 'Open drain needs a cover', 'Drain overflowing'],
    sewer: ['Sewer overflowing on the road', 'Sewer line blocked', 'Bad smell from sewer'],
    waterlogging: ['Waterlogging after rain', 'Water standing on the road'],
    animal: ['Stray cattle blocking the road', 'Aggressive stray dogs', 'Injured stray animal'],
    carcass: ['Dead animal on the road', 'Dead animal near homes'],
    encroachment: ['Footpath encroached by shops', 'Illegal stall blocking the road', 'Construction material on the road'],
    hoarding: ['Illegal hoarding', 'Unsafe hoarding about to fall', 'Posters on public walls'],
    park: ['Park not maintained', 'Broken swings in the park', 'Park lights not working'],
    tree: ['Tree fallen on the road', 'Branches touching power lines', 'Dangerous leaning tree'],
    toilet: ['Public toilet not cleaned', 'Public toilet locked', 'No water in public toilet'],
    mosquito: ['Fogging needed in the area', 'Stagnant water breeding mosquitoes'],
    footpath: ['Broken footpath tiles', 'Footpath missing', 'Footpath slabs uneven']
  };
  const title = form.querySelector('#title'), description = form.querySelector('#description'), photo = form.querySelector('[data-photo-input]');
  const suggestions = form.querySelector('[data-suggestions]'), anonymous = form.querySelector('[data-anonymous]');
  const lat = form.querySelector('#latitude'), lon = form.querySelector('#longitude'), address = form.querySelector('#address');
  const slaNote = form.querySelector('[data-sla-note]'), slaValue = form.querySelector('[data-sla-value]');
  const check = (key, done, value) => {
    const item = form.querySelector(`[data-check="${key}"]`);
    if (!item) return;
    item.classList.toggle('is-done', done);
    if (value !== undefined) item.querySelector('[data-value]').textContent = value;
  };
  const chosen = () => form.querySelector('input[name=categoryId]:checked');

  const renderSuggestions = () => {
    suggestions.replaceChildren();
    const code = chosen()?.dataset.code;
    (SUGGESTIONS[code] || []).forEach(text => {
      const b = document.createElement('button');
      b.type = 'button'; b.className = 'suggestion'; b.textContent = t(text);
      b.addEventListener('click', () => { title.value = t(text); title.dispatchEvent(new Event('input', { bubbles: true })); description.focus(); });
      suggestions.append(b);
    });
  };
  const update = () => {
    const c = chosen();
    check('service', !!c, c ? c.dataset.name : t('Not selected'));
    if (slaNote) { slaNote.hidden = !c; if (c) slaValue.textContent = c.dataset.sla; }
    const detailsDone = title.value.trim().length >= 5 && description.value.trim().length >= 15;
    check('details', detailsDone, detailsDone ? title.value.trim() : t('Not added'));
    const file = photo?.files?.[0];
    check('photo', !!file, file ? file.name : t('Not added'));
    const located = lat.value !== '' && lon.value !== '';
    check('location', located && address.value.trim().length >= 5, located ? (address.value.trim() || t('Pin selected')) : t('Not selected'));
    const item = form.querySelector('[data-check="identity"] [data-value]');
    if (item && anonymous) item.textContent = anonymous.checked ? item.dataset.anon : item.dataset.named;
  };

  form.querySelectorAll('input[name=categoryId]').forEach(r => r.addEventListener('change', () => { renderSuggestions(); update(); }));
  [title, description, address].forEach(el => el?.addEventListener('input', update));
  photo?.addEventListener('change', update);
  anonymous?.addEventListener('change', update);
  document.addEventListener('civic:location', update);
  document.addEventListener('civic:photo-captured', update);
  renderSuggestions();
  update();
})();
