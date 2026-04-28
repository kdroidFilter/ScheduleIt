import React from 'react'
import ReactDOM from 'react-dom/client'
import '@fontsource-variable/fraunces/full.css'
import '@fontsource-variable/fraunces/full-italic.css'
import '@fontsource-variable/inter'
import '@fontsource-variable/jetbrains-mono'
import Contact from './Contact.jsx'
import './styles.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Contact />
  </React.StrictMode>,
)
