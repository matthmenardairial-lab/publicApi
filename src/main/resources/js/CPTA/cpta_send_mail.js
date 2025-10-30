async function traiterStep(data) {
  const obj = {};

  try {
      const updatedSteps = [...data.case.steps];
      const item50 = updatedSteps.findIndex((_step) => _step.id === 50);
      const _step50 = updatedSteps[item50];
      console.log(_step50);
      console.log(_step50.status);

      const invoiceName2 = data.case.case.Title + ".pdf";
      const invoiceUrl2 = data.case.case.FileRef + "/" + invoiceName2;
      const invoiceId2 = await window.get_file_id(data.appState, invoiceUrl2);

      const docData2 = {
          name: invoiceName2,
          template: {
              type: "PDF",
              title: invoiceName2,
              url: invoiceUrl2,
          },
          uniqueId: invoiceId2,
      }
      const attachementList = [];
      attachementList.push(docData2);

      const toEmails = [];
      const email = data.case.case.customProperties.find(
          (prop) => prop.name === "cptaMailoutEmail"
      )?.value;
      console.log("email: ", email);
      if (email !== undefined && email != null && email !== "") {
          toEmails.push(email);
          const templateUrl = "https://mnx4.sharepoint.com/sites/POC-eSign/Templates/Email/sampleMatthieu.html";
          const templateData = {
              name: "Matthieu Ménard",
              date: new Date().toISOString(),
              caseName: data.case.case.Title
          };
          await window.sendEmail(data.appState, "Sujet du Mail", toEmails, attachementList, templateUrl, templateData);
          obj.status = "Email envoyé";
      } else{
          obj.status = "Envoi impossible pas de destinataire";
      }

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
