'use strict';
window.civic={t:key=>document.documentElement.lang==='hi'?(window.CivicHindi?.[key]||key):(window.CivicEnglish?.[key]||key)};
document.getElementById('theme')?.addEventListener('change',e=>document.documentElement.dataset.theme=e.target.value);
const dob=document.getElementById('dateOfBirth');if(dob)dob.max=new Date().toISOString().slice(0,10);

document.body.classList.add('js-enabled');
const menuButton=document.querySelector('.menu-toggle');
if(menuButton)menuButton.addEventListener('click',()=>{const open=menuButton.getAttribute('aria-expanded')!=='true';menuButton.setAttribute('aria-expanded',String(open));document.getElementById('main-nav').classList.toggle('menu-open',open);});
document.querySelectorAll('time[datetime]').forEach(element=>{const value=new Date(element.dateTime);if(!Number.isNaN(value.getTime())){element.textContent=new Intl.DateTimeFormat(document.documentElement.lang,{dateStyle:'medium',timeStyle:'short'}).format(value);element.title=element.dateTime;}});
const departmentSelect=document.getElementById('departmentId'),officerSelect=document.getElementById('officerId');
if(departmentSelect&&officerSelect){const filter=()=>{Array.from(officerSelect.options).forEach(o=>{if(!o.dataset.department)return;o.hidden=o.dataset.department!==departmentSelect.value;o.disabled=o.hidden;});if(officerSelect.selectedOptions[0]?.disabled)officerSelect.value='';};departmentSelect.addEventListener('change',filter);filter();}
document.querySelectorAll('[data-password-target]').forEach(button => {
  button.addEventListener('click', () => {
    const input = document.getElementById(button.dataset.passwordTarget);
    if (!input) return;
    const show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    button.textContent = window.civic.t(show ? 'Hide' : 'Show');
    button.setAttribute('aria-label',window.civic.t(show ? 'Hide password' : 'Show password'));
  });
});
