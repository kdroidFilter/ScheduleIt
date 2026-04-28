import { useEffect, useState } from 'react'
import Background from './Background.jsx'
import LangSwitcher from './LangSwitcher.jsx'
import { translations, detectLang, applyLangAttrs } from './i18n.js'

const BASE = import.meta.env.BASE_URL
const V = `?v=${__BUILD_ID__}`
const asset = (path) => `${BASE}${path}${V}`

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
      <LangSwitcher lang={lang} setLang={setLang} />
      <div className="page">
        <header className="hero">
          <a href={BASE}>
            <img src={asset('icon.png')} alt="ScheduleIt logo" className="logo" />
          </a>
          <h1>{t.privacy.title}</h1>
          <p className="tagline">{t.privacy.lastUpdated}</p>
        </header>

        <article className="legal">
          {t.privacy.sections.map((s) => (
            <section key={s.heading}>
              <h2>{s.heading}</h2>
              <p>{s.body}</p>
            </section>
          ))}
        </article>

        <footer className="footer">
          <a href={BASE}>{t.footer.back}</a>
        </footer>
      </div>
    </>
  )
}
