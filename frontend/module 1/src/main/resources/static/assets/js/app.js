'use strict';
window.civic = {
  lang: document.documentElement.lang === 'hi' ? 'hi' : 'en',
  context: document.body.dataset.context || '',
  icons: document.body.dataset.icons || '',
  t: key => (document.documentElement.lang === 'hi' ? window.CivicHindi : window.CivicEnglish)?.[key] || key,
  /** SVG icon element from the sprite, safe to append. */
  icon(name, cls) {
    const ns = 'http://www.w3.org/2000/svg', svg = document.createElementNS(ns, 'svg'), use = document.createElementNS(ns, 'use');
    svg.setAttribute('class', 'icon' + (cls ? ' ' + cls : ''));
    svg.setAttribute('aria-hidden', 'true');
    use.setAttribute('href', window.civic.icons + '#' + name);
    svg.append(use);
    return svg;
  }
};
(() => {
  const t = window.civic.t, root = document.documentElement;
  document.body.classList.add('js-enabled');

  // Text size (A- / A / A+), remembered on this device.
  const applyFont = size => {
    if (size === 'small' || size === 'large') root.dataset.font = size; else delete root.dataset.font;
    document.querySelectorAll('[data-font-size]').forEach(b => b.setAttribute('aria-pressed', String(b.dataset.fontSize === (size || 'normal'))));
  };
  let savedFont = null;
  try { savedFont = localStorage.getItem('civic_font'); } catch { /* storage unavailable */ }
  applyFont(savedFont);
  document.querySelectorAll('[data-font-size]').forEach(b => b.addEventListener('click', () => {
    applyFont(b.dataset.fontSize);
    try { localStorage.setItem('civic_font', b.dataset.fontSize); } catch { /* storage unavailable */ }
  }));

  // Mobile navigation.
  const menu = document.querySelector('.menu-toggle'), list = document.getElementById('nav-list');
  if (menu && list) menu.addEventListener('click', () => {
    const open = menu.getAttribute('aria-expanded') !== 'true';
    menu.setAttribute('aria-expanded', String(open));
    list.classList.toggle('is-open', open);
  });

  // Theme preview on the account page; date of birth cannot be in the future.
  document.getElementById('theme')?.addEventListener('change', e => { root.dataset.theme = e.target.value; });
  const dob = document.getElementById('dateOfBirth');
  if (dob) dob.max = new Date().toISOString().slice(0, 10);

  // Localised dates: absolute by default, relative ("3 days ago") or date-only on request.
  const locale = window.civic.lang === 'hi' ? 'hi-IN' : 'en-IN';
  const absolute = new Intl.DateTimeFormat(locale, { dateStyle: 'medium', timeStyle: 'short' });
  const dateOnly = new Intl.DateTimeFormat(locale, { dateStyle: 'medium' });
  const relative = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
  const ago = date => {
    const seconds = (date.getTime() - Date.now()) / 1000, abs = Math.abs(seconds);
    for (const [unit, size] of [['year', 31536000], ['month', 2592000], ['week', 604800], ['day', 86400], ['hour', 3600], ['minute', 60]])
      if (abs >= size) return relative.format(Math.round(seconds / size), unit);
    return relative.format(Math.round(seconds), 'second');
  };
  document.querySelectorAll('time[datetime]').forEach(el => {
    const value = new Date(el.dateTime);
    if (Number.isNaN(value.getTime())) return;
    el.textContent = el.hasAttribute('data-relative') ? ago(value) : el.hasAttribute('data-date-only') ? dateOnly.format(value) : absolute.format(value);
    el.title = absolute.format(value);
  });

  // Show / hide password.
  document.querySelectorAll('[data-password-target]').forEach(button => button.addEventListener('click', () => {
    const input = document.getElementById(button.dataset.passwordTarget);
    if (!input) return;
    const show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    button.textContent = t(show ? 'Hide' : 'Show');
    button.setAttribute('aria-label', t(show ? 'Hide password' : 'Show password'));
  }));

  // Officers offered for assignment follow the chosen department.
  const department = document.getElementById('departmentId'), officer = document.getElementById('officerId');
  if (department && officer) {
    const filter = () => {
      Array.from(officer.options).forEach(o => {
        if (!o.dataset.department) return;
        o.hidden = o.dataset.department !== department.value;
        o.disabled = o.hidden;
      });
      if (officer.selectedOptions[0]?.disabled) officer.value = '';
    };
    department.addEventListener('change', filter);
    filter();
  }

  // Character counters.
  document.querySelectorAll('[data-counter]').forEach(input => {
    const out = document.querySelector(`[data-counter-for="${input.id}"]`), max = Number(input.dataset.counter);
    if (!out) return;
    const update = () => { out.textContent = input.value.length + ' / ' + max; out.classList.toggle('is-low', max - input.value.length < 20); };
    input.addEventListener('input', update);
    update();
  });

  // Copy reference, print acknowledgement.
  document.querySelectorAll('[data-copy]').forEach(b => b.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(b.dataset.copy);
      const label = b.lastChild;
      const before = label.textContent;
      label.textContent = b.dataset.copied || t('Copied');
      setTimeout(() => { label.textContent = before; }, 1800);
    } catch { /* clipboard unavailable */ }
  }));
  document.querySelectorAll('[data-print]').forEach(b => b.addEventListener('click', () => window.print()));

  // Demo accounts on the sign-in page.
  document.querySelectorAll('[data-demo-email]').forEach(b => b.addEventListener('click', () => {
    const email = document.getElementById('email'), password = document.getElementById('password');
    if (!email || !password) return;
    email.value = b.dataset.demoEmail;
    password.value = 'CivicDemo!2026';
    email.closest('form')?.querySelector('button[type=submit]')?.focus();
  }));
})();
