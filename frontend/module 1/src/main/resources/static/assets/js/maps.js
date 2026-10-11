'use strict';
(() => {
 const t=window.civic.t, context=document.body.dataset.context || '';
 const usable=p=>p && Number.isFinite(Number(p.latitude)) && Number.isFinite(Number(p.longitude)) && Math.abs(Number(p.latitude))<=90 && Math.abs(Number(p.longitude))<=180;
 const remember=p=>{try{sessionStorage.setItem('civic_location',JSON.stringify(p));}catch{}};
 const recalled=()=>{try{return JSON.parse(sessionStorage.getItem('civic_location'));}catch{return null;}};
 const gps=()=>new Promise((resolve,reject)=>{
  if(!navigator.geolocation){reject(new Error('Location is unavailable. Search an address or choose a map pin.'));return;}
  navigator.geolocation.getCurrentPosition(p=>resolve({latitude:p.coords.latitude,longitude:p.coords.longitude,accuracy:p.coords.accuracy}),
   ()=>reject(new Error('Location could not be accessed. Search an address or choose a map pin.')),{enableHighAccuracy:true,timeout:15000,maximumAge:0});
 });
 const get=async path=>{const response=await fetch(context+path,{credentials:'same-origin',headers:{Accept:'application/json'}});if(!response.ok)throw new Error('Address search is unavailable. Choose a map pin and enter a landmark.');return response.json();};
 const tiles=map=>{
  L.control.zoom({zoomInTitle:t('Zoom in'),zoomOutTitle:t('Zoom out')}).addTo(map);
  const layer=L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'}).addTo(map);
  layer.on('tileerror',()=>document.querySelectorAll('[data-community-message]').forEach(el=>el.textContent=t('Basemap unavailable. The report list and issue pins are still available.')));
 };
 document.querySelectorAll('[data-address-widget]').forEach(widget=>{
  const lat=document.getElementById(widget.dataset.latInput),lon=document.getElementById(widget.dataset.lonInput),address=widget.querySelector('[data-address]'),message=widget.querySelector('[data-map-message]'),list=widget.querySelector('[role=listbox]'),picker=widget.querySelector('[data-location-picker]');
  const isArea=widget.hasAttribute('data-area-widget'),isReport=widget.hasAttribute('data-report-location');
  let map,marker,accuracyCircle,timer,sequence=0,reverseSequence=0,nearSequence=0,controller,pointSource='manual',locating=false;
  const point=()=>lat.value!=='' && lon.value!=='' && usable({latitude:lat.value,longitude:lon.value})?{latitude:Number(lat.value),longitude:Number(lon.value)}:null;
  const status=key=>{if(message)message.textContent=t(key);};
  const closeList=()=>{list.hidden=true;address.setAttribute('aria-expanded','false');};
  const resetLink=()=>{const related=document.getElementById('relatedReportId');if(isReport&&related)related.value='';};
  const nearby=async()=>{
   const panel=document.querySelector('[data-nearby]'),category=document.getElementById('categoryId'),p=point();
   if(!isReport||!panel)return;const seq=++nearSequence;
   if(!p||!category?.value){panel.replaceChildren();return;}
   panel.textContent=t('Checking for similar reports nearby…');
   try{
    const data=await get('/community/nearby?'+new URLSearchParams({categoryId:category.value,latitude:p.latitude,longitude:p.longitude}));if(seq!==nearSequence)return;
    panel.replaceChildren();if(!data.length){panel.textContent=t('No active reports in this category were found nearby.');return;}
    const h=document.createElement('h3');h.textContent=t('Could this be the same issue?');panel.append(h);
    const help=document.createElement('p');help.textContent=t('Link only if you are reporting the exact same issue. Each citizen counts once.');panel.append(help);
    for(const r of data){const row=document.createElement('div');row.className='nearby-match';const a=document.createElement('a');a.href=context+'/community/'+r.id;a.textContent=r.title+' · '+Math.round(r.distanceMeters)+' m';row.append(a);
     if(r.distanceMeters<=150){const button=document.createElement('button');button.type='button';button.className='button button-small button-outline';button.textContent=t('Same issue');button.addEventListener('click',()=>{document.getElementById('relatedReportId').value=String(r.id);panel.querySelectorAll('button').forEach(b=>b.textContent=t('Same issue'));button.textContent=t('Linked to this issue');});row.append(button);}panel.append(row);}
   }catch{if(seq===nearSequence)panel.textContent=t('Nearby suggestions are unavailable. You can still submit the report.');}
  };
  const setPoint=(p,center=true)=>{
   if(!usable(p))return;lat.value=Number(p.latitude).toFixed(6);lon.value=Number(p.longitude).toFixed(6);
   if(map){const location=[Number(lat.value),Number(lon.value)];if(marker)marker.setLatLng(location);else marker=L.marker(location,{draggable:true,icon:L.divIcon({className:'selected-pin',html:'<span></span>',iconSize:[24,24],iconAnchor:[12,12]})}).addTo(map).on('dragend',()=>{const ll=marker.getLatLng();pointSource='manual';resetLink();setPoint({latitude:ll.lat,longitude:ll.lng},false);reverse(point());});
    if(accuracyCircle)map.removeLayer(accuracyCircle);if(p.accuracy)accuracyCircle=L.circle(location,{radius:Math.min(2000,p.accuracy),color:'#2563eb',fillOpacity:.08,weight:1}).addTo(map);if(center)map.setView(location,16);}
   nearby();
  };
  const reverse=async p=>{
   const seq=++reverseSequence;if(!p)return;
   try{const places=await get('/locations/reverse?'+new URLSearchParams({latitude:p.latitude,longitude:p.longitude}));if(seq!==reverseSequence)return;if(places.length)address.value=places[0].label;else status('Pin selected. Add a nearby landmark.');}
   catch{if(seq===reverseSequence)status('Pin selected. Address lookup is unavailable; add a landmark.');}
  };
  if(picker && typeof L!=='undefined'){
   map=L.map(picker,{scrollWheelZoom:false,zoomControl:false}).setView([26.9124,75.7873],13);tiles(map);
   map.on('click',e=>{pointSource='manual';resetLink();setPoint({latitude:e.latlng.lat,longitude:e.latlng.lng},false);status('Location selected. Move the pin if needed.');reverse(point());});
   if(point())setPoint(point());
  }
  address.addEventListener('input',()=>{
   clearTimeout(timer);controller?.abort();const seq=++sequence;++reverseSequence;closeList();resetLink();
   if(pointSource==='address'){lat.value='';lon.value='';if(marker){map.removeLayer(marker);marker=null;}}const q=address.value.trim();if(q.length<3)return;
   timer=setTimeout(async()=>{
    controller=new AbortController();const bias=recalled();const params=new URLSearchParams({q});if(usable(bias)){params.set('latitude',bias.latitude);params.set('longitude',bias.longitude);}
    try{
     const response=await fetch(context+'/locations/search?'+params,{credentials:'same-origin',signal:controller.signal,headers:{Accept:'application/json'}});
     if(!response.ok)throw new Error();const data=await response.json();if(seq!==sequence)return;
     list.replaceChildren();if(!data.length){status('No address matches found. Try a city name or choose a map pin.');return;}
     data.forEach(place=>{const b=document.createElement('button');b.type='button';b.role='option';b.textContent=place.label;b.addEventListener('click',()=>{address.value=place.label;closeList();pointSource='address';setPoint(place);status('Address selected. Check the pin on the map.');if(isArea)widget.requestSubmit();});list.append(b);});list.hidden=false;address.setAttribute('aria-expanded','true');status('Choose an address from the suggestions.');
    }catch(ex){if(ex.name!=='AbortError'&&seq===sequence)status('Address search is unavailable. Choose a map pin and enter a landmark.');}
   },1100);
  });
  address.addEventListener('keydown',e=>{if(e.key==='ArrowDown'&&!list.hidden){e.preventDefault();list.querySelector('button')?.focus();}if(e.key==='Escape')closeList();});
  list.addEventListener('keydown',e=>{const options=Array.from(list.querySelectorAll('button')),i=options.indexOf(document.activeElement);if(e.key==='ArrowDown'||e.key==='ArrowUp'){e.preventDefault();options[(i+(e.key==='ArrowDown'?1:-1)+options.length)%options.length]?.focus();}if(e.key==='Escape'){closeList();address.focus();}});
  document.addEventListener('click',e=>{if(!widget.contains(e.target))closeList();});
  const locate=async()=>{
   if(locating)return;locating=true;status('Requesting your location…');const btn=widget.querySelector('[data-locate]'),submit=isReport?widget.closest('form').querySelector('button[type=submit]'):null;if(btn)btn.disabled=true;if(submit)submit.disabled=true;
   try{const p=await gps();remember(p);pointSource='manual';resetLink();setPoint(p);status('Location selected. Move the pin if needed.');await reverse(p);if(isArea)widget.requestSubmit();}
   catch(ex){status(ex.message);}finally{locating=false;if(btn)btn.disabled=false;if(submit)submit.disabled=false;}
  };
  widget.querySelector('[data-locate]')?.addEventListener('click',locate);
  widget.querySelector('[data-clear-location]')?.addEventListener('click',()=>{lat.value='';lon.value='';address.value='';++reverseSequence;resetLink();if(marker){map.removeLayer(marker);marker=null;}status('Home area cleared.');});
  if(isReport){document.addEventListener('civic:photo-captured',()=>{lat.value='';lon.value='';address.value='';resetLink();++reverseSequence;if(marker){map.removeLayer(marker);marker=null;}locate();});document.getElementById('categoryId')?.addEventListener('change',()=>{resetLink();nearby();});
   const form=widget.closest('form');form.addEventListener('submit',e=>{if(locating){e.preventDefault();status('Wait for the location request to finish.');return;}if(!point()){e.preventDefault();status('Select an address suggestion or choose a map pin before submitting.');address.focus();}});
   // Reuse an explicitly granted location. A new request still obtains fresh coordinates.
   if(!point()&&navigator.permissions)navigator.permissions.query({name:'geolocation'}).then(p=>{if(p.state==='granted')locate();}).catch(()=>{});
  }
  if(isArea){widget.addEventListener('submit',()=>{if(!point()){lat.disabled=true;lon.disabled=true;}});
   if(widget.dataset.hasArea!=='true' && usable(recalled())){setPoint(recalled(),false);widget.requestSubmit();}}
 });
 const welcome=document.querySelector('[data-welcome-location]');if(welcome)welcome.addEventListener('click',async()=>{
  const message=document.querySelector('[data-welcome-message]');welcome.disabled=true;
  try{const p=await gps();remember(p);message.textContent=t('Location enabled for this session. You can change it when reporting.');}
  catch(ex){message.textContent=t(ex.message);}finally{welcome.disabled=false;}
 });
 const community=document.querySelector('[data-community-map]');if(community && typeof L!=='undefined'){
  const map=L.map(community,{scrollWheelZoom:false,zoomControl:false}).setView([26.9124,75.7873],13);tiles(map);const entries=[],bounds=[];
  const colors={high:'#dc2626',medium:'#d99a08',low:'#16835b'};
  document.querySelectorAll('[data-community-report]').forEach(card=>{
   const point=[Number(card.dataset.latitude),Number(card.dataset.longitude)];bounds.push(point);const color=colors[card.dataset.priority]||colors.low;
   const area=L.circle(point,{radius:75,color,fillColor:color,fillOpacity:.13,weight:1}).addTo(map);
   const pin=L.marker(point,{icon:L.divIcon({className:'priority-pin '+card.dataset.priority,html:'<span>'+Number(card.dataset.count)+'</span>',iconSize:[32,32],iconAnchor:[16,16]})}).addTo(map);
   const box=document.createElement('div'),title=document.createElement('strong'),state=document.createElement('p'),link=document.createElement('a');title.textContent=card.dataset.title;state.textContent=t(card.dataset.status)+' · '+t(card.dataset.priority)+' '+t('priority');link.href=context+'/community/'+card.id.replace('report-','');link.textContent=t('View progress →');box.append(title,state,link);pin.bindPopup(box);
   pin.on('click',()=>{document.querySelectorAll('.community-card.is-focused').forEach(el=>el.classList.remove('is-focused'));card.classList.add('is-focused');});entries.push({card,pin,area});
  });
  const lat=community.dataset.latitude,lon=community.dataset.longitude;
  if(lat!==''&&lon!==''&&usable({latitude:lat,longitude:lon})){
   const p=[Number(lat),Number(lon)];L.circleMarker(p,{radius:6,color:'#fff',fillColor:'#2563eb',fillOpacity:1,weight:2}).addTo(map).bindTooltip(t('Selected area'));
   map.fitBounds(L.circle(p,{radius:Number(community.dataset.radius)*1000}).getBounds(),{padding:[20,20]});
  }else if(bounds.length)map.fitBounds(bounds,{padding:[35,35],maxZoom:15});
  document.querySelector('[data-priority-filter]')?.addEventListener('change',e=>entries.forEach(({card,pin,area})=>{
   const show=e.target.value==='all'||card.dataset.priority===e.target.value;card.hidden=!show;
   if(show){pin.addTo(map);area.addTo(map);}else{map.removeLayer(pin);map.removeLayer(area);}
  }));
 }
})();
