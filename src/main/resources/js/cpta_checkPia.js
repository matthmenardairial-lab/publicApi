async function traiterStep(data) {
  const obj = {};
  if (data.step.status === "approved") {
      const case_customProperties = data.case.case.customProperties
      console.log("case_customProperties", case_customProperties);
      const piaNb = data.case.case.customProperties.find(
          (prop) => prop.name === "cptaMailoutPiaNumber"
      )?.value;
      console.log("piaNb", piaNb);
      if (piaNb === null || piaNb === undefined || piaNb === "") {
          alert("Please complete the PIA number before continue the process")
          const json = { ...data.case };
          console.log("json", json);

          const updatedSteps = [...data.case.steps];
          const item400 = updatedSteps.findIndex((_step) => _step.id === 400);
          const _step400 = updatedSteps[item400];
          console.log(">>> _step400",_step400);
          _step400.isChecked = false;
          _step400.checkedBy = "";
          _step400.checkedDate = undefined;
          _step400.status = null;
          updatedSteps[item400] = { ...updatedSteps[item400], ..._step400 };
          console.log(">>> updatedSteps",updatedSteps);

          await window.updateJson(data.appState, json, {
              isNew: false,
              tempFileName: "checklist.json",
              fileName: "checklist.json",
              destination: data.case.case?.FileRef,
          });
          return obj;
      }
  }

}
// Expose en global
window.traiterStep = traiterStep;
