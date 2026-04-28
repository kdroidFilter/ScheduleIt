export const LANGS = [
  { code: 'en', label: 'EN' },
  { code: 'fr', label: 'FR' },
  { code: 'he', label: 'עב' },
]

export const RTL_LANGS = ['he']

export const translations = {
  en: {
    tagline: 'Plan your week at a glance — fast, native, cross-platform.',
    releaseNotes: 'Release notes',
    gallery: {
      editEvent: 'Edit events with title, time, color and notes',
      settings: 'Tune visible hours and per-day schedules',
    },
    features: {
      title: 'Your school week, finally under control',
      lede: 'ScheduleIt was made for the way students actually plan: classes that repeat every week, exams that sneak up on you, and a homework pile that never sleeps. Drop your timetable in once and let the rest of the year run itself.',
      items: [
        {
          icon: '🎒',
          title: 'Built for student life',
          body: 'From morning math to late-night study sessions, organize every class, lab and lecture in a layout that mirrors your real school week.',
        },
        {
          icon: '📚',
          title: 'Subjects, color-coded',
          body: 'Give each course its own color so you can tell English from Chemistry at a glance — no more mixing up rooms or showing up to the wrong class.',
        },
        {
          icon: '🔔',
          title: 'Never miss a deadline',
          body: 'Track exams, homework and project drop-offs alongside your regular schedule. Your agenda stays the single source of truth from Monday to Friday.',
        },
        {
          icon: '🏫',
          title: 'Like the school planner, but better',
          body: 'All the structure of the paper agenda your teachers told you to keep updated — minus the smudged ink, lost pages and forgotten Mondays.',
        },
        {
          icon: '⏰',
          title: 'Custom hours per day',
          body: 'Half-day on Wednesday? Late start on Friday? Tune visible hours per day so your week looks exactly like your real timetable.',
        },
        {
          icon: '☕',
          title: 'Study breaks included',
          body: 'Block out lunch, recess and free periods so you can actually see when you have time to breathe between two pop quizzes.',
        },
      ],
    },
    download: {
      heading: 'Download',
      loading: 'Loading latest release…',
      error: 'Could not load release: ',
      detected: 'Detected',
      forYou: 'For your machine',
    },
    footer: {
      source: 'View source on GitHub',
      privacy: 'Privacy policy',
      back: 'Back to home',
    },
    os: {
      windows: 'Windows',
      mac: 'macOS',
      linux: 'Linux',
      other: 'Other',
    },
    privacy: {
      title: 'Privacy policy',
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
    tagline: "Planifiez votre semaine d'un coup d'œil — rapide, natif, multi-plateforme.",
    releaseNotes: 'Notes de version',
    gallery: {
      editEvent: 'Modifiez vos événements : titre, horaire, couleur et notes',
      settings: "Personnalisez les heures visibles et l'emploi du temps par jour",
    },
    features: {
      title: 'Votre semaine scolaire enfin sous contrôle',
      lede: "ScheduleIt a été pensé pour la façon dont les élèves planifient vraiment : des cours qui se répètent chaque semaine, des contrôles qui arrivent par surprise et une pile de devoirs qui ne dort jamais. Entrez votre emploi du temps une fois, et laissez le reste de l'année se gérer toute seule.",
      items: [
        {
          icon: '🎒',
          title: 'Conçu pour la vie étudiante',
          body: 'Des maths du matin aux révisions tardives, organisez chaque cours, TP et conférence dans une vue qui colle à votre vraie semaine.',
        },
        {
          icon: '📚',
          title: 'Matières en couleurs',
          body: "Donnez une couleur à chaque cours pour distinguer l'anglais de la chimie d'un coup d'œil — fini les confusions de salle ou les oublis.",
        },
        {
          icon: '🔔',
          title: 'Ne ratez plus une échéance',
          body: 'Suivez contrôles, devoirs et rendus de projets en parallèle de votre emploi du temps. Votre agenda devient la seule source de vérité du lundi au vendredi.',
        },
        {
          icon: '🏫',
          title: "Comme l'agenda papier, mais en mieux",
          body: "Toute la structure de l'agenda papier que vos profs vous demandent de tenir à jour — sans l'encre qui bave, les pages perdues ou les lundis oubliés.",
        },
        {
          icon: '⏰',
          title: 'Horaires personnalisés par jour',
          body: 'Demi-journée le mercredi ? Début tardif le vendredi ? Réglez les heures visibles par jour pour que votre semaine ressemble à votre vrai emploi du temps.',
        },
        {
          icon: '☕',
          title: 'Pauses incluses',
          body: 'Bloquez la pause déjeuner, la récré et les heures libres pour voir quand vous pouvez vraiment souffler entre deux interros surprises.',
        },
      ],
    },
    download: {
      heading: 'Télécharger',
      loading: 'Chargement de la dernière version…',
      error: 'Impossible de charger la version : ',
      detected: 'Détecté',
      forYou: 'Pour votre machine',
    },
    footer: {
      source: 'Voir le code source sur GitHub',
      privacy: 'Politique de confidentialité',
      back: "Retour à l'accueil",
    },
    os: {
      windows: 'Windows',
      mac: 'macOS',
      linux: 'Linux',
      other: 'Autre',
    },
    privacy: {
      title: 'Politique de confidentialité',
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
          body: "En cas de mise à jour de cette politique, la nouvelle version sera publiée sur cette page avec une nouvelle date de révision.",
        },
        {
          heading: 'Contact',
          body: "Pour toute question relative à cette politique de confidentialité, ouvrez une issue sur le dépôt GitHub.",
        },
      ],
    },
  },
  he: {
    tagline: 'תכננו את השבוע במבט אחד — מהיר, נטיבי, חוצה פלטפורמות.',
    releaseNotes: 'הערות גרסה',
    gallery: {
      editEvent: 'עריכת אירועים — כותרת, שעה, צבע והערות',
      settings: 'התאמת שעות נראות ולוח שבועי לכל יום',
    },
    features: {
      title: 'השבוע הלימודי שלכם — סוף סוף תחת שליטה',
      lede: 'ScheduleIt נבנה בדיוק כמו שתלמידים באמת מתכננים: שיעורים שחוזרים בכל שבוע, מבחנים שצצים מאיפה שלא מצפים, וערימה של שיעורי בית שלעולם לא ישנה. הכניסו את מערכת השעות פעם אחת — והשנה תרוץ מעצמה.',
      items: [
        {
          icon: '🎒',
          title: 'בנוי לחיי הסטודנט',
          body: 'ממתמטיקה בבוקר ועד למידה בלילה — סדרו כל שיעור, מעבדה והרצאה במבט שמשקף את השבוע האמיתי שלכם.',
        },
        {
          icon: '📚',
          title: 'מקצועות בקוד צבעים',
          body: 'תנו לכל מקצוע צבע משלו, כדי להבחין בין אנגלית לכימיה במבט מהיר — בלי בלבולי כיתות או הופעה לשיעור הלא נכון.',
        },
        {
          icon: '🔔',
          title: 'אף פעם לא תפספסו תאריך יעד',
          body: 'עקבו אחרי מבחנים, שיעורי בית והגשות פרויקטים לצד מערכת השעות הרגילה. היומן הוא המקור היחיד לאמת מיום ראשון עד שישי.',
        },
        {
          icon: '🏫',
          title: 'כמו יומן בית הספר — רק יותר טוב',
          body: 'כל הסדר של היומן הנייר שהמורים מבקשים לעדכן — בלי דיו מרוח, דפים אבודים או ימי שני שנשכחו.',
        },
        {
          icon: '⏰',
          title: 'שעות מותאמות לכל יום',
          body: 'חצי יום ביום רביעי? התחלה מאוחרת ביום שישי? כווננו את השעות לכל יום כך שהשבוע ייראה בדיוק כמו במציאות.',
        },
        {
          icon: '☕',
          title: 'כולל הפסקות',
          body: 'חסמו הפסקת צהריים, הפסקות וזמנים פנויים, כדי לראות מתי באמת יש זמן לנשום בין שני מבחני פתע.',
        },
      ],
    },
    download: {
      heading: 'הורדה',
      loading: 'טוען את הגרסה האחרונה…',
      error: 'טעינת הגרסה נכשלה: ',
      detected: 'זוהה',
      forYou: 'מתאים למכשיר שלך',
    },
    footer: {
      source: 'צפו בקוד המקור ב-GitHub',
      privacy: 'מדיניות פרטיות',
      back: 'חזרה לדף הבית',
    },
    os: {
      windows: 'Windows',
      mac: 'macOS',
      linux: 'Linux',
      other: 'אחר',
    },
    privacy: {
      title: 'מדיניות פרטיות',
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
