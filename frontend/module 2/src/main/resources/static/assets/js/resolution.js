'use strict';
document.querySelectorAll('[data-comparison]').forEach(panel=>{
 const slider=panel.querySelector('input[type=range]'),layer=panel.querySelector('.before-layer');
 if(slider&&layer){const change=()=>{layer.style.clipPath=`inset(0 ${100-Number(slider.value)}% 0 0)`;};slider.addEventListener('input',change);change();}
});
