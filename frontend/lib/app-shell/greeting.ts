/** "Good morning, Priya" — time-of-day + first name. */
export function greeting(
  fullName: string | undefined,
  now: Date = new Date(),
): string {
  const hour = now.getHours();
  const partOfDay = hour < 12 ? "morning" : hour < 18 ? "afternoon" : "evening";
  const firstName = (fullName ?? "").trim().split(/\s+/)[0];
  return firstName ? `Good ${partOfDay}, ${firstName}` : `Good ${partOfDay}`;
}
