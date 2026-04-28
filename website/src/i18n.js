export const LANGS = [
  { code: 'en', label: 'EN' },
  { code: 'fr', label: 'FR' },
  { code: 'he', label: 'עב' },
]

export const RTL_LANGS = ['he']

export const translations = {
  en: {
    tagline: 'A calmer week, in one glance.',
    releaseNotes: 'Release notes',
    foss: 'Free, open source',
    truths: [
      {
        eyebrow: 'Native everywhere',
        title: 'On every screen <em>you own</em>.',
        body: 'Mac, Windows, Linux, Android, tablet. The same week, wherever you open it. No browser tab, no fan spinning up.',
        devices: {
          tablet: 'screens/tablet-day.webp',
          phone: 'screens/mobile-day.webp',
          altTablet: 'ScheduleIt on a tablet — weekly view',
          altPhone: 'ScheduleIt on Android — daily view',
        },
      },
      {
        eyebrow: 'Yours, on your machine',
        title: 'Your week stays <em>private</em>.',
        body: 'Local-first by design. Your file lives on your disk. Optional Drive backup if you want it, nothing if you do not.',
        visual: 'screens/light.webp',
        alt: 'ScheduleIt desktop — weekly view',
      },
      {
        eyebrow: 'Built around your day',
        title: 'Tuned to <em>how you live</em>.',
        body: 'Half-day Wednesday. Late Friday. Color per subject. Visible hours per day. The agenda matches your week, not the other way around.',
        visual: 'screens/mobile-settings.webp',
        alt: 'Settings — tune visible hours and per-day schedules',
      },
    ],
    download: {
      eyebrow: 'Download',
      heading: 'Get it now. <em>It is free.</em>',
      loading: 'Loading latest release…',
      error: 'Could not load release: ',
      primaryFor: 'Download for',
      otherShow: 'Other platforms',
      otherHide: 'Hide other platforms',
    },
    footer: {
      source: 'Source',
      privacy: 'Privacy',
      contact: 'Contact',
      back: '← Back',
      by: 'by Elie Gambache',
      support: 'Support',
      kofiAria: 'Support me on Ko-fi',
    },
    contact: {
      title: 'Say <em>hello</em>.',
      lede: 'Open source projects, collaborations, feedback — anything that does not need a meeting works here.',
      links: {
        portfolio: { label: 'Portfolio', desc: 'Other projects and writings' },
        email: { label: 'Email', desc: 'Direct line, no funnel' },
        github: { label: 'GitHub', desc: 'Code, issues, contributions' },
        linkedin: { label: 'LinkedIn', desc: 'Professional profile' },
      },
    },
    os: {
      windows: 'Windows',
      mac: 'macOS',
      linux: 'Linux',
      other: 'Other',
    },
    privacy: {
      title: 'Privacy',
      lastUpdated: 'Last updated: April 28, 2026',
      sections: [
        {
          heading: 'Summary',
          body: 'ScheduleIt is a local-first application. Your schedule, events and settings stay on your device. We do not run servers that store your data, we do not have user accounts, and we do not collect analytics or telemetry.',
        },
        {
          heading: 'Data stored on your device',
          body: 'Schedules, events, colors, visible hours and other preferences are saved locally on the device where the app is installed. Uninstalling the app or clearing its data removes this information. Backing up or syncing this data between your devices is your responsibility.',
        },
        {
          heading: 'This website',
          body: 'This website is a static page hosted on GitHub Pages. It uses your browser\'s local storage only to remember your preferred interface language. It does not use cookies, advertising trackers or analytics scripts.',
        },
        {
          heading: 'Third-party requests',
          body: 'To display the latest version and download links, the website calls the public GitHub Releases API for the kdroidFilter/ScheduleIt repository. As the operator of GitHub Pages and the GitHub API, GitHub may log your IP address and User-Agent. Their handling of this data is governed by GitHub\'s own privacy policy.',
        },
        {
          heading: 'Children',
          body: 'ScheduleIt is designed for students and is safe to use by minors: since no data is collected, no personal information is transmitted off the device.',
        },
        {
          heading: 'Changes',
          body: 'If this policy changes, the updated version will be published on this page with a new revision date.',
        },
        {
          heading: 'Contact',
          body: 'For questions about this privacy policy, please open an issue on the GitHub repository.',
        },
      ],
    },
  },
  fr: {
    tagline: "Une semaine plus calme, d'un seul coup d'œil.",
    releaseNotes: 'Notes de version',
    foss: 'Gratuit, open source',
    truths: [
      {
        eyebrow: 'Natif partout',
        title: 'Sur chaque écran <em>qui est à toi</em>.',
        body: 'Mac, Windows, Linux, Android, tablette. La même semaine, où que tu ouvres l\'app. Pas d\'onglet, pas de ventilo qui s\'emballe.',
        devices: {
          tablet: 'screens/tablet-day-fr.webp',
          phone: 'screens/mobile-day.webp',
          altTablet: 'ScheduleIt sur tablette — vue hebdomadaire',
          altPhone: 'ScheduleIt sur Android — vue du jour',
        },
      },
      {
        eyebrow: 'Chez toi, sur ta machine',
        title: 'Ta semaine reste <em>privée</em>.',
        body: 'Local-first par principe. Ton fichier vit sur ton disque. Sauvegarde Drive en option si tu veux, rien sinon.',
        visual: 'screens/light.webp',
        alt: 'ScheduleIt desktop — vue hebdomadaire',
      },
      {
        eyebrow: 'Calé sur ta journée',
        title: 'Réglé sur <em>ta vraie semaine</em>.',
        body: 'Demi-journée le mercredi. Vendredi tardif. Une couleur par matière. Heures visibles par jour. L\'agenda s\'adapte, pas l\'inverse.',
        visual: 'screens/mobile-settings.webp',
        alt: 'Réglages — heures visibles et plages horaires par jour',
      },
    ],
    download: {
      eyebrow: 'Télécharger',
      heading: 'Installe-le. <em>C\'est gratuit.</em>',
      loading: 'Chargement de la dernière version…',
      error: 'Impossible de charger la version : ',
      primaryFor: 'Télécharger pour',
      otherShow: 'Autres plateformes',
      otherHide: 'Masquer les autres plateformes',
    },
    footer: {
      source: 'Source',
      privacy: 'Confidentialité',
      contact: 'Contact',
      back: '← Retour',
      by: 'par Elie Gambache',
      support: 'Soutenir',
      kofiAria: 'Soutenez-moi sur Ko-fi',
    },
    contact: {
      title: 'Dis <em>bonjour</em>.',
      lede: 'Projets open source, collaborations, retours : tout ce qui ne demande pas de réunion passe par ici.',
      links: {
        portfolio: { label: 'Portfolio', desc: 'Autres projets et écrits' },
        email: { label: 'Email', desc: 'Ligne directe, sans détour' },
        github: { label: 'GitHub', desc: 'Code, issues, contributions' },
        linkedin: { label: 'LinkedIn', desc: 'Profil professionnel' },
      },
    },
    os: {
      windows: 'Windows',
      mac: 'macOS',
      linux: 'Linux',
      other: 'Autre',
    },
    privacy: {
      title: 'Confidentialité',
      lastUpdated: 'Dernière mise à jour : 28 avril 2026',
      sections: [
        {
          heading: 'En résumé',
          body: "ScheduleIt est une application local-first. Votre emploi du temps, vos événements et vos paramètres restent sur votre appareil. Nous n'exploitons aucun serveur qui stocke vos données, il n'y a pas de compte utilisateur, et aucune analyse ni télémétrie n'est collectée.",
        },
        {
          heading: 'Données stockées sur votre appareil',
          body: "Les emplois du temps, événements, couleurs, heures visibles et autres préférences sont enregistrés localement sur l'appareil où l'application est installée. Désinstaller l'application ou en effacer les données supprime ces informations. La sauvegarde ou la synchronisation entre vos appareils est de votre responsabilité.",
        },
        {
          heading: 'Ce site web',
          body: "Ce site est une page statique hébergée sur GitHub Pages. Il utilise le stockage local de votre navigateur uniquement pour mémoriser votre langue d'interface. Il n'utilise ni cookies, ni traceurs publicitaires, ni scripts d'analyse.",
        },
        {
          heading: 'Requêtes externes',
          body: "Pour afficher la dernière version et les liens de téléchargement, le site interroge l'API publique GitHub Releases du dépôt kdroidFilter/ScheduleIt. En tant qu'opérateur de GitHub Pages et de l'API GitHub, GitHub peut journaliser votre adresse IP et votre User-Agent. Leur traitement est régi par la politique de confidentialité de GitHub.",
        },
        {
          heading: 'Mineurs',
          body: "ScheduleIt est conçu pour les élèves et son utilisation par des mineurs est sûre : puisqu'aucune donnée n'est collectée, aucune information personnelle ne quitte l'appareil.",
        },
        {
          heading: 'Modifications',
          body: 'En cas de mise à jour de cette politique, la nouvelle version sera publiée sur cette page avec une nouvelle date de révision.',
        },
        {
          heading: 'Contact',
          body: 'Pour toute question relative à cette politique de confidentialité, ouvrez une issue sur le dépôt GitHub.',
        },
      ],
    },
  },
  he: {
    tagline: 'שבוע רגוע יותר, במבט אחד.',
    releaseNotes: 'הערות גרסה',
    foss: 'חינם, קוד פתוח',
    truths: [
      {
        eyebrow: 'נטיבי בכל מקום',
        title: 'בכל מסך <em>שיש לך</em>.',
        body: 'Mac, Windows, Linux, Android, טאבלט. אותו שבוע, איפה שלא תפתח. בלי טאב, בלי מאוורר שמתעורר.',
        devices: {
          tablet: 'screens/tablet-day.webp',
          phone: 'screens/mobile-day.webp',
          altTablet: 'ScheduleIt על טאבלט — תצוגה שבועית',
          altPhone: 'ScheduleIt על Android — תצוגת היום',
        },
      },
      {
        eyebrow: 'אצלך, על המכשיר שלך',
        title: 'השבוע שלך נשאר <em>פרטי</em>.',
        body: 'Local-first מההתחלה. הקובץ שלך חי על הדיסק שלך. גיבוי ל-Drive אם תרצה, כלום אם לא.',
        visual: 'screens/light.webp',
        alt: 'תצוגה שבועית של ScheduleIt — דסקטופ',
      },
      {
        eyebrow: 'מותאם ליום שלך',
        title: 'מכוון <em>לאיך שאתה חי</em>.',
        body: 'חצי יום ביום רביעי. יום שישי מאוחר. צבע למקצוע. שעות נראות לכל יום. היומן מתאים לחיים שלך, לא להפך.',
        visual: 'screens/mobile-settings.webp',
        alt: 'הגדרות — שעות נראות ולוחות יומיים',
      },
    ],
    download: {
      eyebrow: 'הורדה',
      heading: 'תורידו עכשיו. <em>זה חינם.</em>',
      loading: 'טוען את הגרסה האחרונה…',
      error: 'טעינת הגרסה נכשלה: ',
      primaryFor: 'הורדה ל-',
      otherShow: 'פלטפורמות נוספות',
      otherHide: 'הסתרת פלטפורמות נוספות',
    },
    footer: {
      source: 'קוד מקור',
      privacy: 'פרטיות',
      contact: 'יצירת קשר',
      back: '→ חזרה',
      by: 'על ידי Elie Gambache',
      support: 'תמיכה',
      kofiAria: 'תמכו בי ב-Ko-fi',
    },
    contact: {
      title: 'תגידו <em>שלום</em>.',
      lede: 'פרויקטי קוד פתוח, שיתופי פעולה, משוב — כל מה שלא דורש פגישה עובר כאן.',
      links: {
        portfolio: { label: 'תיק עבודות', desc: 'פרויקטים ופרסומים נוספים' },
        email: { label: 'אימייל', desc: 'קו ישיר, בלי מסננים' },
        github: { label: 'GitHub', desc: 'קוד, issues ותרומות' },
        linkedin: { label: 'LinkedIn', desc: 'פרופיל מקצועי' },
      },
    },
    os: {
      windows: 'Windows',
      mac: 'macOS',
      linux: 'Linux',
      other: 'אחר',
    },
    privacy: {
      title: 'פרטיות',
      lastUpdated: 'עודכן לאחרונה: 28 באפריל 2026',
      sections: [
        {
          heading: 'תקציר',
          body: 'ScheduleIt היא אפליקציה שפועלת במקור באופן מקומי. מערכת השעות, האירועים וההגדרות שלך נשמרים במכשיר שלך בלבד. איננו מפעילים שרתים שמאחסנים את הנתונים שלך, אין חשבונות משתמש, ואיננו אוספים ניתוחים או טלמטריה.',
        },
        {
          heading: 'נתונים השמורים במכשיר שלך',
          body: 'מערכות שעות, אירועים, צבעים, שעות נראות והעדפות נוספות נשמרים מקומית במכשיר שבו האפליקציה מותקנת. הסרת האפליקציה או מחיקת הנתונים שלה מסירה גם מידע זה. גיבוי או סנכרון של נתונים אלו בין המכשירים שלך — באחריותך.',
        },
        {
          heading: 'אתר זה',
          body: 'אתר זה הוא דף סטטי המתארח ב-GitHub Pages. הוא עושה שימוש באחסון המקומי של הדפדפן רק כדי לזכור את שפת הממשק המועדפת עליך. הוא אינו עושה שימוש בקובצי Cookie, בכלי מעקב פרסומיים או בסקריפטי ניתוח.',
        },
        {
          heading: 'בקשות לצדדים שלישיים',
          body: 'כדי להציג את הגרסה האחרונה ואת קישורי ההורדה, האתר קורא ל-API הציבורי של GitHub Releases עבור המאגר kdroidFilter/ScheduleIt. בתור מפעילת GitHub Pages וה-API של GitHub, GitHub עשויה לתעד את כתובת ה-IP ואת ה-User-Agent שלך. הטיפול בנתונים אלו כפוף למדיניות הפרטיות של GitHub.',
        },
        {
          heading: 'קטינים',
          body: 'ScheduleIt מיועדת לתלמידים ובטוחה לשימוש על ידי קטינים: מאחר שלא נאסף שום מידע, אף פרט אישי לא יוצא מהמכשיר.',
        },
        {
          heading: 'שינויים',
          body: 'אם מדיניות זו תשתנה, הגרסה המעודכנת תתפרסם בדף זה עם תאריך עדכון חדש.',
        },
        {
          heading: 'יצירת קשר',
          body: 'לשאלות בנוגע למדיניות פרטיות זו, נא לפתוח issue במאגר GitHub.',
        },
      ],
    },
  },
}

export function detectLang() {
  if (typeof window === 'undefined') return 'en'
  const stored = localStorage.getItem('lang')
  if (stored && translations[stored]) return stored
  const browser = (navigator.language || 'en').slice(0, 2).toLowerCase()
  if (browser === 'iw') return 'he'
  return translations[browser] ? browser : 'en'
}

export function applyLangAttrs(lang) {
  if (typeof document === 'undefined') return
  document.documentElement.lang = lang
  document.documentElement.dir = RTL_LANGS.includes(lang) ? 'rtl' : 'ltr'
}
