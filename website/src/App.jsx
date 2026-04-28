import { useEffect, useMemo, useRef, useState } from 'react'
import Background from './Background.jsx'
import LangSwitcher from './LangSwitcher.jsx'
import SupportLink from './SupportLink.jsx'
import Footer from './Footer.jsx'
import { OS_ICON_PATHS } from './osIcons.js'
import { translations, detectLang, applyLangAttrs } from './i18n.js'

const REPO = 'kdroidFilter/ScheduleIt'
const API_URL = `https://api.github.com/repos/${REPO}/releases?per_page=10`
const BASE = import.meta.env.BASE_URL
const V = `?v=${__BUILD_ID__}`
const asset = (path) => `${BASE}${path}${V}`

function detectOS() {
  if (typeof navigator === 'undefined') return 'unknown'
  const ua = navigator.userAgent.toLowerCase()
  // Android UA also contains "linux" — check it first.
  if (ua.includes('android')) return 'android'
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
  if (n.endsWith('.yml') || n.endsWith('.yaml') || n.endsWith('.blockmap')) return null
  if (n.endsWith('.zip')) return null
  if (n.endsWith('.aab')) return null // Play Store bundle, not user-installable
  if (n.endsWith('.exe')) return 'windows'
  if (n.endsWith('.dmg') || n.endsWith('.pkg')) return 'mac'
  if (n.endsWith('.deb') || n.endsWith('.rpm') || n.endsWith('.appimage')) return 'linux'
  if (n.endsWith('.apk')) return 'android'
  return 'other'
}

function classifyArch(name) {
  const n = name.toLowerCase()
  if (n.includes('arm64') || n.includes('aarch64')) return 'arm64'
  if (n.includes('amd64') || n.includes('x64') || n.includes('x86_64')) return 'x64'
  return null
}

function prettyExt(name) {
  return name.slice(name.lastIndexOf('.') + 1).toUpperCase()
}

function prettyArch(name) {
  const arch = classifyArch(name)
  if (arch === 'arm64') return 'ARM64'
  if (arch === 'x64') return 'x64'
  return ''
}

function formatSize(bytes) {
  const mb = bytes / (1024 * 1024)
  return `${mb.toFixed(1)} MB`
}

function osLabel(os) {
  if (os === 'mac') return 'macOS'
  if (os === 'windows') return 'Windows'
  if (os === 'linux') return 'Linux'
  if (os === 'android') return 'Android'
  return 'Other'
}

function OsIcon({ os }) {
  const path = OS_ICON_PATHS[os]
  if (!path) return null
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d={path} />
    </svg>
  )
}

function pickPrimary(assets, os, arch) {
  if (!assets) return null
  const candidates = assets.filter((a) => classify(a.name) === os)
  if (!candidates.length) return null
  if (arch) {
    const match = candidates.find((a) => classifyArch(a.name) === arch)
    if (match) return match
  }
  // Prefer the canonical user-installable extension per OS.
  const prefer = { mac: '.dmg', windows: '.exe', linux: '.appimage', android: '.apk' }[os]
  if (prefer) {
    const match = candidates.find((a) => a.name.toLowerCase().endsWith(prefer))
    if (match) return match
  }
  return candidates[0]
}

// Aggregate assets across the most recent releases. Dedupe by (os, arch, ext)
// so we only keep the most recent build for each distinct combo — desktop
// builds lag behind Android-only releases, but we still want one entry each.
function assetSignature(name) {
  const ext = name.slice(name.lastIndexOf('.')).toLowerCase()
  const lower = name.toLowerCase()
  let arch = 'any'
  if (lower.includes('arm64') || lower.includes('aarch64')) arch = 'arm64'
  else if (lower.includes('amd64') || lower.includes('x64') || lower.includes('x86_64')) arch = 'x64'
  return `${ext}|${arch}`
}

function aggregateReleases(releases) {
  if (!Array.isArray(releases) || !releases.length) return null
  const seen = new Map()
  for (const r of releases) {
    if (r.draft || r.prerelease) continue
    for (const a of r.assets || []) {
      const sig = assetSignature(a.name)
      if (!seen.has(sig)) {
        seen.set(sig, { ...a, release_tag: r.tag_name, release_url: r.html_url })
      }
    }
  }
  const top = releases.find((r) => !r.draft && !r.prerelease) || releases[0]
  return {
    tag_name: top.tag_name,
    html_url: top.html_url,
    assets: Array.from(seen.values()),
  }
}

export default function App() {
  const [release, setRelease] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [arch, setArch] = useState(null)
  const [lang, setLang] = useState(detectLang)
  const [showOther, setShowOther] = useState(false)
  const detectedOS = useMemo(detectOS, [])
  const t = translations[lang]
  const otherRef = useRef(null)

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
      .then((data) => setRelease(aggregateReleases(data)))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const grouped = useMemo(() => {
    if (!release?.assets) return {}
    return release.assets.reduce((map, a) => {
      const os = classify(a.name)
      if (!os) return map
      if (!map[os]) map[os] = []
      map[os].push(a)
      return map
    }, {})
  }, [release])

  const primary = useMemo(
    () => (release && detectedOS !== 'unknown' ? pickPrimary(release.assets, detectedOS, arch) : null),
    [release, detectedOS, arch]
  )

  const otherOses = ['mac', 'windows', 'linux', 'android', 'other'].filter(
    (os) => os !== detectedOS && grouped[os]?.length
  )

  return (
    <>
      <Background />
      <div className="chrome">
        <SupportLink lang={lang} />
        <LangSwitcher lang={lang} setLang={setLang} />
      </div>
      <div className="page">
        <header className="hero">
          <div className="hero-mark">
            <img src={asset('icon.png')} alt="" className="hero-logo" />
            <h1 className="wordmark">ScheduleIt</h1>
          </div>
          <div className="hero-aside">
            <p className="tagline">{t.tagline}</p>
            <div className="version-row">
              <span className="foss-pill">{t.foss}</span>
              {release && (
                <>
                  <span className="version-tag">{release.tag_name}</span>
                  <a className="version-link" href={release.html_url} target="_blank" rel="noreferrer">
                    {t.releaseNotes}
                  </a>
                </>
              )}
            </div>
          </div>
        </header>

        <section className="showcase">
          <img
            src={asset('screens/light.webp')}
            alt="ScheduleIt — weekly view"
            className="hero-shot"
          />
        </section>

        <section className="truths">
          {t.truths.map((truth, i) => (
            <article key={i} className="truth">
              <div className="truth-text">
                <p className="truth-eyebrow">{truth.eyebrow}</p>
                <h2
                  className="truth-title"
                  dangerouslySetInnerHTML={{ __html: truth.title }}
                />
                <p className="truth-body">{truth.body}</p>
              </div>
              {truth.devices ? (
                <figure className="truth-visual truth-visual-devices">
                  <img
                    className="device-tablet"
                    src={asset(truth.devices.tablet)}
                    alt={truth.devices.altTablet}
                    loading="lazy"
                  />
                  <img
                    className="device-phone"
                    src={asset(truth.devices.phone)}
                    alt={truth.devices.altPhone}
                    loading="lazy"
                  />
                </figure>
              ) : (
                <figure className="truth-visual">
                  <img src={asset(truth.visual)} alt={truth.alt} loading="lazy" />
                </figure>
              )}
            </article>
          ))}
        </section>

        <section className="download">
          <p className="download-eyebrow">{t.download.eyebrow}</p>
          <h2
            className="download-heading"
            dangerouslySetInnerHTML={{ __html: t.download.heading }}
          />

          {loading && <p className="dl-status">{t.download.loading}</p>}
          {error && <p className="dl-status error">{t.download.error}{error}</p>}

          {primary && (
            <a className="dl-primary" href={primary.browser_download_url}>
              <span className="dl-primary-os">
                <OsIcon os={detectedOS} />
                <span>{t.download.primaryFor} {osLabel(detectedOS)}</span>
              </span>
              <span className="dl-primary-meta">
                {prettyExt(primary.name)}
                {prettyArch(primary.name) && ` · ${prettyArch(primary.name)}`}
                {' · '}
                {formatSize(primary.size)}
              </span>
            </a>
          )}

          {release && otherOses.length > 0 && (
            <div className="dl-disclosure">
              <button
                type="button"
                className="dl-toggle"
                aria-expanded={showOther}
                aria-controls="dl-other"
                onClick={() => setShowOther((v) => !v)}
              >
                <span>{showOther ? t.download.otherHide : t.download.otherShow}</span>
                <svg className="chev" viewBox="0 0 12 12" aria-hidden="true">
                  <path d="M2 4l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </button>

              {showOther && (
                <div className="dl-other" id="dl-other" ref={otherRef}>
                  {otherOses.map((os) => (
                    <div key={os} className="dl-os">
                      <span className="dl-os-name">
                        <OsIcon os={os} />
                        {t.os[os]}
                      </span>
                      <ul className="dl-list">
                        {grouped[os].map((a) => (
                          <li key={a.id}>
                            <a className="dl-row" href={a.browser_download_url}>
                              <span className="dl-row-label">
                                {prettyExt(a.name)}
                                {prettyArch(a.name) && ` · ${prettyArch(a.name)}`}
                              </span>
                              <span className="dl-row-size">{formatSize(a.size)}</span>
                            </a>
                          </li>
                        ))}
                      </ul>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </section>

        <Footer lang={lang} />
      </div>
    </>
  )
}
