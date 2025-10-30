async function traiterStep(data) {
  const obj = {};

      const case_customProperties = data.case.case.customProperties
      console.log("case_customProperties", case_customProperties);

    const mpooFormName = "Template_MPOO.docx";
    const mpooFormUrl = data.case.case.FileRef + "/" + mpooFormName;
    const mpooFormId = await window.get_file_id(data.appState, mpooFormUrl);
    console.log(">>> mpooFormName", mpooFormName);
    console.log(">>> mpooFormUrl", mpooFormUrl);
    console.log(">>> mpooFormId", mpooFormId);

    const docData = {
        name: mpooFormName,
        template: {
            type: "PDF",
            title: mpooFormName,
            url: mpooFormUrl,
        },
        uniqueId: mpooFormId,
    }
    console.log(">>> Begin renameFile");
    await window.renameFile(data.appState, docData, data.case.case.Title + ".docx");
    console.log(">>> End renameFile");

}
// Expose en global
window.traiterStep = traiterStep;
