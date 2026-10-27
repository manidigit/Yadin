# GPT Glossy SVG icons

24 standalone SVG assets for a consistent blue/cyan/violet glossy icon family, scoped to the GPT Glossy theme.

Files: home.svg, book.svg, refresh.svg, library.svg, chart.svg, settings.svg, calendar.svg, target.svg, check.svg, close.svg, speaker.svg, bulb.svg, number.svg, leaf.svg, flame.svg, brain.svg, trophy.svg, search.svg, add.svg, edit.svg, delete.svg, save.svg, filter.svg, translate.svg

- SVG viewBox: 24×24; intrinsic size: 96×96; transparent background.
- Styling: cool blue-to-violet gradient, cyan highlights, rounded strokes and soft drop shadow.
- No external fonts, scripts, raster assets, or network dependencies.
- Keep these assets separate from Gemini's icon set.

Android note: Android does not natively decode SVG through ImageVector. Convert these files to VectorDrawable or use an approved SVG renderer before wiring them into Compose. This commit adds assets only; it does not change app behavior or Gemini.
