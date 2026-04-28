import { useEffect, useMemo, useState } from 'react'
import Background from './Background.jsx'
import { OS_ICON_PATHS } from './osIcons.js'
import { LANGS, translations, detectLang, applyLangAttrs } from './i18n.js'

const REPO = 'kdroidFilter/ScheduleIt'
const API_URL = `https://api.github.com/repos/${REPO}/releases/latest`
const BASE = import.meta.env.BASE_URL
const V = `?v=${__BUILD_ID__}`
const asset = (path) => `${BASE}${path}${V}`

function detectOS() {
  if (typeof navigator === 'undefined') return 'unknown'
  const ua = navigator.userAgent.toLowerCase()
  if (ua.includes('win')) return 'windows'
  if (ua.includes('mac')) return 'mac'
  if (ua.includes('linux')) return 'linux'
  return 'unknown'
}

async function detectArch() {
  if (typeof navigator === 'undefined') return null
  if (navigator.userAgentData?.getHighEntropyValues) {
    try {
      const data = await navigator.userAgentData.getHighEntropyValues(['architecture', 'bitness'])
      if (data.architecture === 'arm' && data.bitness === '64') return 'arm64'
      if (data.architecture === 'x86' && data.bitness === '64') return 'x64'
    } catch {
      // fall through to UA fallback
    }
  }
  const ua = navigator.userAgent.toLowerCase()
  if (ua.includes('arm64') || ua.includes('aarch64')) return 'arm64'
  if (ua.includes('x86_64') || ua.includes('x64') || ua.includes('amd64') || ua.includes('win64')) return 'x64'
  return null
}

function classify(name) {
  const n = name.toLowerCase()
  if (n.endsWith('.appx') || n.endsWith('.msixbundle')) return null
  if (n.endsWith('.exe')) return 'windows'
  if (n.includes('mac') || n.endsWith('.dmg') || n.endsWith('.pkg')) return 'mac'
  if (n.includes('linux') || n.endsWith('.deb') || n.endsWith('.rpm') || n.endsWith('.appimage')) return 'linux'
  return 'other'
}

function classifyArch(name) {
  const n = name.toLowerCase()
  if (n.includes('arm64') || n.includes('aarch64')) return 'arm64'
  if (n.includes('amd64') || n.includes('x64') || n.includes('x86_64')) return 'x64'
  return null
}

function prettyLabel(name) {
  const arch = classifyArch(name)
  const ext = name.slice(name.lastIndexOf('.') + 1).toUpperCase()
  return [ext, arch === 'arm64' ? 'ARM64' : arch === 'x64' ? 'x64' : '']
    .filter(Boolean)
    .join(' · ')
}

function formatSize(bytes) {
  const mb = bytes / (1024 * 1024)
  return `${mb.toFixed(1)} MB`
}

function OsIcon({ os }) {
  const path = OS_ICON_PATHS[os]
  if (!path) {
    return (
      <svg className="os-icon" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M21 16.5c0 .38-.21.71-.53.88l-7.9 4.44c-.16.12-.36.18-.57.18-.21 0-.41-.06-.57-.18l-7.9-4.44A.991.991 0 013 16.5v-9c0-.38.21-.71.53-.88l7.9-4.44c.16-.12.36-.18.57-.18.21 0 .41.06.57.18l7.9 4.44c.32.17.53.5.53.88v9M12 4.15L6.04 7.5 12 10.85 17.96 7.5 12 4.15M5 15.91l6 3.38v-6.71L5 9.21v6.7m14 0v-6.7l-6 3.37v6.71l6-3.38z" />
      </svg>
    )
  }
  return (
    <svg className="os-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d={path} />
    </svg>
  )
}

function LangSwitcher({ lang, setLang }) {
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

export default function App() {
  const [release, setRelease] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [arch, setArch] = useState(null)
  const [lang, setLang] = useState(detectLang)
  const detectedOS = useMemo(detectOS, [])
  const t = translations[lang]

  useEffect(() => {
    applyLangAttrs(lang)
    try {
      localStorage.setItem('lang', lang)
    } catch {
      // localStorage may be unavailable (private mode, etc.) — ignore
    }
  }, [lang])

  useEffect(() => {
    detectArch().then(setArch)
  }, [])

  useEffect(() => {
    fetch(API_URL)
      .then((r) => {
        if (!r.ok) throw new Error(`GitHub API ${r.status}`)
        return r.json()
      })
      .then(setRelease)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const grouped = useMemo(() => {
    if (!release?.assets) return {}
    const acc = release.assets.reduce((map, a) => {
      const os = classify(a.name)
      if (!os) return map
      if (!map[os]) map[os] = []
      map[os].push(a)
      return map
    }, {})
    if (arch) {
      Object.keys(acc).forEach((os) => {
        acc[os].sort((a, b) => {
          const aMatch = classifyArch(a.name) === arch
          const bMatch = classifyArch(b.name) === arch
          if (aMatch && !bMatch) return -1
          if (!aMatch && bMatch) return 1
          return 0
        })
      })
    }
    return acc
  }, [release, arch])

  const order = ['windows', 'mac', 'linux', 'other'].sort((a, b) => {
    if (a === detectedOS) return -1
    if (b === detectedOS) return 1
    return 0
  })

  const GALLERY = [
    { src: asset('screens/edit-event.jpg'), caption: t.gallery.editEvent },
    { src: asset('screens/settings.jpg'), caption: t.gallery.settings },
  ]

  return (
    <>
      <Background />
      <LangSwitcher lang={lang} setLang={setLang} />
      <div className="page">

      <header className="hero">
        <img src={asset('icon.png')} alt="ScheduleIt logo" className="logo" />
        <h1>ScheduleIt</h1>
        <p className="tagline">{t.tagline}</p>
        {release && (
          <div className="version">
            <span className="badge">{release.tag_name}</span>
            <a href={release.html_url} target="_blank" rel="noreferrer">{t.releaseNotes}</a>
          </div>
        )}
      </header>

      <section className="showcase">
        <img
          src={asset('screens/light.jpg')}
          alt="ScheduleIt weekly overview"
          className="hero-shot"
        />
      </section>

      <section className="gallery">
        {GALLERY.map((g) => (
          <figure key={g.src}>
            <img src={g.src} alt={g.caption} loading="lazy" />
            <figcaption>{g.caption}</figcaption>
          </figure>
        ))}
      </section>

      <section className="features">
        <h2>{t.features.title}</h2>
        <p className="features-lede">{t.features.lede}</p>
        <div className="feature-grid">
          {t.features.items.map((f) => (
            <article key={f.title} className="feature-card">
              <div className="feature-icon">{f.icon}</div>
              <h3>{f.title}</h3>
              <p>{f.body}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="download-section">
        <h2>{t.download.heading}</h2>
        {loading && <p className="status">{t.download.loading}</p>}
        {error && <p className="status error">{t.download.error}{error}</p>}

        {release && (
          <div className="downloads">
            {order
              .filter((os) => grouped[os]?.length)
              .map((os) => (
                <section key={os} className={`os-card ${os === detectedOS ? 'highlight' : ''}`}>
                  <header>
                    <OsIcon os={os} />
                    <h3>{t.os[os]}</h3>
                    {os === detectedOS && <span className="pill">{t.download.detected}</span>}
                  </header>
                  <ul>
                    {grouped[os].map((a) => {
                      const matchArch = arch && classifyArch(a.name) === arch && os === detectedOS
                      return (
                        <li key={a.id}>
                          <a className={`dl-btn ${matchArch ? 'match' : ''}`} href={a.browser_download_url}>
                            <span className="dl-name">
                              {prettyLabel(a.name)}
                              {matchArch && <span className="dl-tag">{t.download.forYou}</span>}
                            </span>
                            <span className="dl-size">{formatSize(a.size)}</span>
                          </a>
                        </li>
                      )
                    })}
                  </ul>
                </section>
              ))}
          </div>
        )}
      </section>

      <footer className="footer">
        <a href={`https://github.com/${REPO}`} target="_blank" rel="noreferrer">
          {t.footer.source}
        </a>
      </footer>
      </div>
    </>
  )
}
