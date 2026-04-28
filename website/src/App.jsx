import { useEffect, useMemo, useState } from 'react'

const REPO = 'kdroidFilter/ScheduleIt'
const API_URL = `https://api.github.com/repos/${REPO}/releases/latest`

function detectOS() {
  if (typeof navigator === 'undefined') return 'unknown'
  const ua = navigator.userAgent.toLowerCase()
  if (ua.includes('win')) return 'windows'
  if (ua.includes('mac')) return 'mac'
  if (ua.includes('linux')) return 'linux'
  return 'unknown'
}

function classify(name) {
  const n = name.toLowerCase()
  if (n.endsWith('.appx') || n.endsWith('.msixbundle')) return null
  if (n.endsWith('.exe')) return 'windows'
  if (n.includes('mac') || n.endsWith('.dmg') || n.endsWith('.pkg')) return 'mac'
  if (n.includes('linux') || n.endsWith('.deb') || n.endsWith('.rpm') || n.endsWith('.appimage')) return 'linux'
  return 'other'
}

function prettyLabel(name) {
  const lower = name.toLowerCase()
  const arch =
    lower.includes('arm64') ? 'ARM64' :
    lower.includes('amd64') || lower.includes('x64') || lower.includes('x86_64') ? 'x64' :
    ''
  const ext = name.slice(name.lastIndexOf('.') + 1).toUpperCase()
  return [ext, arch].filter(Boolean).join(' · ')
}

function formatSize(bytes) {
  const mb = bytes / (1024 * 1024)
  return `${mb.toFixed(1)} MB`
}

const OS_META = {
  windows: { label: 'Windows', icon: '🪟' },
  mac: { label: 'macOS', icon: '🍎' },
  linux: { label: 'Linux', icon: '🐧' },
  other: { label: 'Other', icon: '📦' },
}

export default function App() {
  const [release, setRelease] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const detected = useMemo(detectOS, [])

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
    return release.assets.reduce((acc, a) => {
      const os = classify(a.name)
      if (!os) return acc
      if (!acc[os]) acc[os] = []
      acc[os].push(a)
      return acc
    }, {})
  }, [release])

  const order = ['windows', 'mac', 'linux', 'other'].sort((a, b) => {
    if (a === detected) return -1
    if (b === detected) return 1
    return 0
  })

  return (
    <div className="page">
      <header className="hero">
        <h1>ScheduleIt</h1>
        <p className="tagline">Schedule your week. Download the desktop app.</p>
        {release && (
          <div className="version">
            <span className="badge">{release.tag_name}</span>
            <a href={release.html_url} target="_blank" rel="noreferrer">Release notes</a>
          </div>
        )}
      </header>

      {loading && <p className="status">Loading latest release…</p>}
      {error && <p className="status error">Could not load release: {error}</p>}

      {release && (
        <main className="downloads">
          {order
            .filter((os) => grouped[os]?.length)
            .map((os) => (
              <section key={os} className={`os-card ${os === detected ? 'highlight' : ''}`}>
                <header>
                  <span className="os-icon">{OS_META[os].icon}</span>
                  <h2>{OS_META[os].label}</h2>
                  {os === detected && <span className="pill">Detected</span>}
                </header>
                <ul>
                  {grouped[os].map((asset) => (
                    <li key={asset.id}>
                      <a className="dl-btn" href={asset.browser_download_url}>
                        <span className="dl-name">{prettyLabel(asset.name)}</span>
                        <span className="dl-size">{formatSize(asset.size)}</span>
                      </a>
                    </li>
                  ))}
                </ul>
              </section>
            ))}
        </main>
      )}

      <footer className="footer">
        <a href={`https://github.com/${REPO}`} target="_blank" rel="noreferrer">
          View source on GitHub
        </a>
      </footer>
    </div>
  )
}
