import { translations } from './i18n.js'

const REPO = 'kdroidFilter/ScheduleIt'
const KOFI_URL = 'https://ko-fi.com/lomityaesh'
const BASE = import.meta.env.BASE_URL

export default function Footer({ lang }) {
  const t = translations[lang]
  return (
    <footer className="footer">
      <div className="footer-links">
        <a href={`https://github.com/${REPO}`} target="_blank" rel="noreferrer">
          {t.footer.source}
        </a>
        <a href={`${BASE}contact.html`}>{t.footer.contact}</a>
        <a href={`${BASE}privacy.html`}>{t.footer.privacy}</a>
      </div>
      <div className="footer-by">
        <a
          className="kofi-heart"
          href={KOFI_URL}
          target="_blank"
          rel="noreferrer"
          aria-label={t.footer.kofiAria}
          title={t.footer.kofiAria}
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
          </svg>
        </a>
        <span>{t.footer.by}</span>
      </div>
    </footer>
  )
}
