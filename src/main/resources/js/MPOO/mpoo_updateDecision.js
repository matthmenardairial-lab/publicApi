async function traiterStep(data) {
  const obj = {};

    console.log("script mpoo_updateDecision execution");
    const updatedSteps = [...data.case.steps];
    const user = await window.getCurrentUser(data.appState);
    console.log("user", user);
    const item250 = updatedSteps.findIndex((_step) => _step.id === 250);
    const _step250 = updatedSteps[item250];

}
// Expose en global
window.traiterStep = traiterStep;
