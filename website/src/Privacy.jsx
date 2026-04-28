import { useEffect, useState } from 'react'
import Background from './Background.jsx'
import LangSwitcher from './LangSwitcher.jsx'
import SupportLink from './SupportLink.jsx'
import Footer from './Footer.jsx'
import { translations, detectLang, applyLangAttrs } from './i18n.js'

const BASE = import.meta.env.BASE_URL

export default function Privacy() {
  const [lang, setLang] = useState(detectLang)
  const t = translations[lang]

  useEffect(() => {
    applyLangAttrs(lang)
    try {
      localStorage.setItem('lang', lang)
    } catch {
      // localStorage may be unavailable (private mode, etc.) — ignore
    }
    document.title = `ScheduleIt — ${t.privacy.title}`
  }, [lang, t.privacy.title])

  return (
    <>
      <Background />
      <div className="chrome">
        <SupportLink lang={lang} />
        <LangSwitcher lang={lang} setLang={setLang} />
      </div>
      <div className="page">
        <header className="sub-header">
          <h1>{t.privacy.title}</h1>
          <p>{t.privacy.lastUpdated}</p>
        </header>

        <article className="legal">
          {t.privacy.sections.map((s) => (
            <section key={s.heading}>
              <h2>{s.heading}</h2>
              <p>{s.body}</p>
            </section>
          ))}
        </article>

        <div className="back-link">
          <a href={BASE}>{t.footer.back}</a>
        </div>

        <Footer lang={lang} />
      </div>
    </>
  )
}
