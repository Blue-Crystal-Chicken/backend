// ============================================================================
//  Scope di sede del Manager.
//  Il Manager vede SOLO la propria sede: queste funzioni leggono la sede dal
//  profilo utente persistito al login (locationId/locationName dal backend).
// ============================================================================

function readUser() {
  try {
    return JSON.parse(localStorage.getItem("bcc_user") || "null");
  } catch {
    return null;
  }
}

// id della sede del manager (null se non assegnata)
export function managerLocationId() {
  const u = readUser();
  return u?.locationId ?? null;
}

export function managerLocationName() {
  const u = readUser();
  return u?.locationName ?? null;
}
