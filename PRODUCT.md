# Product

## Register

brand

## Users

Indie makers, designers, developers, students, and detail-oriented professionals who download desktop and mobile software directly. They land on the site already vaguely interested (link from GitHub, Reddit, HN, a friend) and decide in 10 seconds whether ScheduleIt feels like *their kind of tool*. They use Mac, Windows, Linux, or Android. They read English, French, or Hebrew. The job-to-be-done on this page is: **decide to install**.

## Product Purpose

ScheduleIt is a free, open-source, native calendar / scheduling app available on every platform. The website's only job is to make a visitor want to install it — the design IS the marketing. Success = installs per visit, not time-on-page or scroll depth. Secondary: signal that this is crafted, indie, trustworthy (not abandonware, not SaaS-bait, not trying to upsell).

## Brand Personality

**Crafted. Calm. Generous.** The voice of a maker who has spent more time on the kerning than on the funnel. Confident without bragging. Warm, not corporate. The kind of tool you keep open all day because it feels good to look at. Premium-indie — closer to *Things 3* / *Cron* than to *Google Calendar* / *Outlook*.

The visitor should feel a quiet pull: *"I want this on my machine."* Not hype, not urgency — desire through craft.

## Anti-references

- **Generic SaaS landing template.** Hero + 3-card features grid + pricing + CTA. Vercel/Stripe-clone aesthetics applied to a calendar app.
- **Dark-blue dev-tool reflex.** Linear/Raycast/Supabase visual shorthand. ScheduleIt is for everyone, not only terminal-dwellers, and the category already over-uses this palette.
- **App-store download wall.** Long platform list dominating the page. Downloads are a destination, not the hero.
- **Gradient text on headings, glassmorphism, side-stripe accents, hero-metric template.** Banned by impeccable laws — also the current site uses gradient-text on the H1, which we will remove.
- **Identical card grids** for features, screenshots, downloads. Repetitive rectangles = template energy.
- **Crypto / AI-hype** aesthetic: neon on near-black, glow halos, "future of X" copy.

## Design Principles

1. **Desire over description.** The page seduces; it does not explain. Cut copy until only the irresistible bits remain. Screenshots and silence carry more than bullet lists.
2. **Show, don't tell.** Real product shots at large size, breathing, asymmetric. The app must look so good people install before reading.
3. **Escape the category palette.** Warm, daylight-leaning surface with one committed accent. No dark blue, no neon, no "tech".
4. **Editorial typography over UI typography.** A confident serif display (*Fraunces* / *Instrument Serif* family) paired with a precise grotesque body. The wordmark earns its size; nothing else competes.
5. **Generous whitespace, asymmetric rhythm.** Vary spacing and width. No section feels like the previous one stamped again.
6. **Trust through craft, not badges.** No "as seen on", no fake testimonials, no star counts at the top. The design itself is the proof of care.

## Accessibility & Inclusion

- Target WCAG AA on text and interactive elements; AAA where it costs nothing.
- Full RTL support (Hebrew is a first-class language). Layout, mirroring, and motion direction must respect `dir`.
- Respect `prefers-reduced-motion` — disable parallax, scroll-driven, and decorative motion entirely when set.
- Color is never the only signal (focus rings, underlines, icons reinforce state).
- Do not lock interaction behind hover. Touch and keyboard parity required.
