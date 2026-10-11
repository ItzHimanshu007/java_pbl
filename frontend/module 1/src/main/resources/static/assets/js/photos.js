'use strict';
(() => {
 const t=window.civic.t;
 document.querySelectorAll('[data-photo-widget]').forEach(widget=>{
  const input=widget.querySelector('[data-photo-input]'),img=widget.querySelector('[data-photo-preview]'),figure=widget.querySelector('.photo-preview'),caption=widget.querySelector('[data-photo-caption]'),message=widget.querySelector('[data-photo-message]'),remove=widget.querySelector('[data-remove-photo]'),camera=widget.querySelector('[data-camera]'),native=widget.querySelector('[data-native-camera]'),dialog=widget.querySelector('dialog'),video=dialog?.querySelector('video');
  let url,stream;
  const preview=()=>{
   if(url)URL.revokeObjectURL(url);url=null;const file=input.files[0];figure.hidden=!file;remove.hidden=!file;
   if(!file){img.removeAttribute('src');caption.textContent='';return;}
   if(!['image/png','image/jpeg'].includes(file.type)||file.size>5*1024*1024){input.value='';figure.hidden=true;remove.hidden=true;message.textContent=t('Choose a PNG or JPEG photograph smaller than 5 MB.');return;}
   url=URL.createObjectURL(file);img.src=url;caption.textContent=file.name+' · '+(file.size/1024/1024).toFixed(2)+' MB';message.textContent=t('Photo selected. Check the preview before submitting.');
  };
  input.addEventListener('change',preview);remove.addEventListener('click',()=>{input.value='';preview();message.textContent=t('Photo removed.');});
  const useFile=file=>{const transfer=new DataTransfer();transfer.items.add(file);input.files=transfer.files;preview();if(input.files.length)document.dispatchEvent(new CustomEvent('civic:photo-captured'));};
  const stop=()=>{stream?.getTracks().forEach(track=>track.stop());stream=null;if(video)video.srcObject=null;if(dialog?.open)dialog.close();};
  camera.addEventListener('click',async()=>{
   if(!dialog||!navigator.mediaDevices?.getUserMedia){native.click();return;}
   try{stream=await navigator.mediaDevices.getUserMedia({video:{facingMode:{ideal:'environment'},width:{ideal:1920}},audio:false});video.srcObject=stream;dialog.showModal();await video.play();}
   catch{stop();message.textContent=t('Camera is unavailable. Choose a photograph from your device.');native.click();}
  });
  native.addEventListener('change',()=>{if(native.files[0])useFile(native.files[0]);native.value='';});
  dialog?.querySelector('[data-close-camera]')?.addEventListener('click',stop);
  dialog?.addEventListener('cancel',stop);window.addEventListener('pagehide',()=>{stop();if(url)URL.revokeObjectURL(url);});
  dialog?.querySelector('[data-shutter]')?.addEventListener('click',()=>{
   if(!video.videoWidth){message.textContent=t('Wait for the camera to be ready.');return;}
   const ratio=Math.min(1,2400/video.videoWidth,2400/video.videoHeight),canvas=document.createElement('canvas');canvas.width=Math.round(video.videoWidth*ratio);canvas.height=Math.round(video.videoHeight*ratio);canvas.getContext('2d').drawImage(video,0,0,canvas.width,canvas.height);
   canvas.toBlob(blob=>{if(blob){useFile(new File([blob],'civicpulse-photo.jpg',{type:'image/jpeg'}));stop();}},'image/jpeg',.88);
  });
 });
})();
