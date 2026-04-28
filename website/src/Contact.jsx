import { useEffect, useState } from 'react'
import Background from './Background.jsx'
import LangSwitcher from './LangSwitcher.jsx'
import SupportLink from './SupportLink.jsx'
import Footer from './Footer.jsx'
import { translations, detectLang, applyLangAttrs } from './i18n.js'

const BASE = import.meta.env.BASE_URL
const V = `?v=${__BUILD_ID__}`
const asset = (path) => `${BASE}${path}${V}`

const ICONS = {
  portfolio: (
    <path d="M3 6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V6zm3-1a1 1 0 0 0-1 1v3h14V6a1 1 0 0 0-1-1H6zm13 6H5v7a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-7zM7 14a1 1 0 0 1 1-1h4a1 1 0 1 1 0 2H8a1 1 0 0 1-1-1z" />
  ),
  email: (
    <path d="M3 5h18a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1zm1 2.4V18h16V7.4l-7.45 5.21a1 1 0 0 1-1.1 0L4 7.4zm15.3-.4H4.7L12 12.1 19.3 7z" />
  ),
  github: (
    <path d="M12 .5C5.65.5.5 5.65.5 12c0 5.08 3.29 9.39 7.86 10.91.58.11.79-.25.79-.56 0-.27-.01-1-.02-1.97-3.2.7-3.87-1.54-3.87-1.54-.52-1.32-1.27-1.67-1.27-1.67-1.04-.71.08-.7.08-.7 1.15.08 1.76 1.18 1.76 1.18 1.02 1.75 2.68 1.24 3.34.95.1-.74.4-1.24.73-1.53-2.55-.29-5.24-1.28-5.24-5.69 0-1.26.45-2.28 1.18-3.09-.12-.29-.51-1.46.11-3.05 0 0 .97-.31 3.18 1.18.92-.26 1.91-.39 2.89-.39.98 0 1.97.13 2.89.39 2.21-1.49 3.18-1.18 3.18-1.18.62 1.59.23 2.76.11 3.05.74.81 1.18 1.83 1.18 3.09 0 4.42-2.69 5.39-5.25 5.68.41.36.78 1.06.78 2.13 0 1.54-.01 2.79-.01 3.17 0 .31.21.68.8.56C20.21 21.39 23.5 17.08 23.5 12 23.5 5.65 18.35.5 12 .5z" />
  ),
  linkedin: (
    <path d="M20.45 20.45h-3.55v-5.57c0-1.33-.03-3.04-1.85-3.04-1.86 0-2.14 1.45-2.14 2.94v5.67H9.36V9h3.41v1.56h.05c.48-.9 1.64-1.85 3.37-1.85 3.6 0 4.27 2.37 4.27 5.46v6.28zM5.34 7.43a2.06 2.06 0 1 1 0-4.13 2.06 2.06 0 0 1 0 4.13zm1.78 13.02H3.55V9h3.57v11.45zM22.22 0H1.77C.79 0 0 .77 0 1.73v20.54C0 23.23.79 24 1.77 24h20.45C23.2 24 24 23.23 24 22.27V1.73C24 .77 23.2 0 22.22 0z" />
  ),
}

const LINK_ORDER = ['portfolio', 'email', 'github', 'linkedin']

const URLS = {
  portfolio: 'https://eliegambache.kdroidfilter.com/',
  email: 'mailto:elyahou.hadass@gmail.com',
  github: 'https://github.com/kdroidFilter',
  linkedin: 'https://www.linkedin.com/in/elie-gambache-525917355',
}

export default function Contact() {
  const [lang, setLang] = useState(detectLang)
  const t = translations[lang]

  useEffect(() => {
    applyLangAttrs(lang)
    try {
      localStorage.setItem('lang', lang)
    } catch {
      // localStorage may be unavailable (private mode, etc.) — ignore
    }
    document.title = `ScheduleIt — ${t.contact.title}`
  }, [lang, t.contact.title])

  return (
    <>
      <Background />
      <SupportLink lang={lang} />
      <LangSwitcher lang={lang} setLang={setLang} />
      <div className="page">
        <header className="hero">
          <a href={BASE}>
            <img src={asset('icon.png')} alt="ScheduleIt logo" className="logo" />
          </a>
          <h1>{t.contact.title}</h1>
          <p className="tagline">{t.contact.lede}</p>
        </header>

        <section className="contact-grid">
          {LINK_ORDER.map((key) => {
            const item = t.contact.links[key]
            const isExternal = !URLS[key].startsWith('mailto:')
            return (
              <a
                key={key}
                className="contact-card"
                href={URLS[key]}
                {...(isExternal ? { target: '_blank', rel: 'noreferrer' } : {})}
              >
                <span className={`contact-icon contact-icon-${key}`}>
                  <svg viewBox="0 0 24 24" aria-hidden="true">{ICONS[key]}</svg>
                </span>
                <span className="contact-text">
                  <span className="contact-label">{item.label}</span>
                  <span className="contact-desc">{item.desc}</span>
                </span>
              </a>
            )
          })}
        </section>

        <div className="back-link">
          <a href={BASE}>{t.footer.back}</a>
        </div>

        <Footer lang={lang} />
      </div>
    </>
  )
}
