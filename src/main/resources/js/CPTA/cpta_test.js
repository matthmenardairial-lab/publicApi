async function traiterStep(data) {
  const obj = {};
  try {
      console.log(">>> Begenning of the script cpta_checkBookmark");
      console.log(">>> data", data);
      const updatedSteps = [...data.case.steps];
      console.log(">>> updatedSteps",updatedSteps);
      const item500 = updatedSteps.findIndex((_step) => _step.id === 500);
      const _step500 = updatedSteps[item500];
      console.log(">>> _step500", _step500);

      const createdComment = {
          "date": new Date().toDateString(),
          "name" : "Mathieu Menard",
          "email": "Mathieu.Menard@inetum-realdolmen.world",
          "text": "Test Matthieu",
      }

      console.log(">>> _step500.comments", _step500.comments);
      if (!_step500.comments) {
          console.log("Passe dans le if du _step100.comments !== undefined");
          const _step500_Comments = [];
          _step500_Comments.push(createdComment);
          _step500.comments = _step500_Comments;
      } else {
          const _step500_Comments = [..._step500.comments];
          _step500_Comments.push(createdComment);
          _step500.comments = _step500_Comments;
      }
      updatedSteps[item500] = { ...updatedSteps[item500], ..._step500 };

      const json = { ...data.case };
      json.steps = updatedSteps;

      console.log("Json", json);
      console.log("Json", json.toString());
      await window.updateJson(data.appState, json, {
          isNew: false,
          tempFileName: "checklist.json",
          fileName: "checklist.json",
          destination: data.case.case?.FileRef,
      });

        obj.status = "Step modified";
        return obj;
  } catch (err) {
    console.error(err);
    obj.status = "erreur";
    obj.error = err.message;
    return obj;
  }
}
// Expose en global
window.traiterStep = traiterStep;
