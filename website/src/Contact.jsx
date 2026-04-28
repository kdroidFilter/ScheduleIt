import { useEffect, useState } from 'react'
import Background from './Background.jsx'
import LangSwitcher from './LangSwitcher.jsx'
import SupportLink from './SupportLink.jsx'
import Footer from './Footer.jsx'
import { translations, detectLang, applyLangAttrs } from './i18n.js'

const BASE = import.meta.env.BASE_URL

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
    document.title = `ScheduleIt — ${t.contact.title.replace(/<[^>]+>/g, '')}`
  }, [lang, t.contact.title])

  return (
    <>
      <Background />
      <div className="chrome">
        <SupportLink lang={lang} />
        <LangSwitcher lang={lang} setLang={setLang} />
      </div>
      <div className="page">
        <header className="sub-header">
          <h1 dangerouslySetInnerHTML={{ __html: t.contact.title }} />
          <p>{t.contact.lede}</p>
        </header>

        <ul className="contact-list">
          {LINK_ORDER.map((key, i) => {
            const item = t.contact.links[key]
            const isExternal = !URLS[key].startsWith('mailto:')
            const num = String(i + 1).padStart(2, '0')
            return (
              <li key={key}>
                <a
                  className="contact-row"
                  href={URLS[key]}
                  {...(isExternal ? { target: '_blank', rel: 'noreferrer' } : {})}
                >
                  <span className="contact-key">{num} / {item.label}</span>
                  <span className="contact-name">{item.label}</span>
                  <span className="contact-desc">{item.desc}</span>
                  <svg className="contact-arrow" viewBox="0 0 18 18" aria-hidden="true">
                    <path d="M3 9h12M10 4l5 5-5 5" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </a>
              </li>
            )
          })}
        </ul>

        <div className="back-link">
          <a href={BASE}>{t.footer.back}</a>
        </div>

        <Footer lang={lang} />
      </div>
    </>
  )
}
