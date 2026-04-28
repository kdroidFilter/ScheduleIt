import { LANGS } from './i18n.js'

export default function LangSwitcher({ lang, setLang }) {
  return (
    <div className="lang-switcher" role="group" aria-label="Language">
      {LANGS.map((l) => (
        <button
          key={l.code}
          type="button"
          className={l.code === lang ? 'active' : ''}
          onClick={() => setLang(l.code)}
          aria-pressed={l.code === lang}
        >
          {l.label}
        </button>
      ))}
    </div>
  )
}
