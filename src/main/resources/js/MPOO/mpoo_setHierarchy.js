async function traiterStep(data) {
  const obj = {};
  console.log(">>> Begin mpoo_setHierarchy execution");
    const user = await window.getCurrentUser(data.appState);
    console.log("user", user);

    const allHierarchy = await window.getHierarchy(data.appState, user);
    console.log(">>> allHierarchy", allHierarchy);
}
// Expose en global
window.traiterStep = traiterStep;
