async function traiterStep(data) {
  const obj = {};
  if (data.step.status === "approved") {
      try {
          const updatedSteps = [...data.case.steps];
          const item50 = updatedSteps.findIndex((_step) => _step.id === 50);
          const _step50 = updatedSteps[item50];
          console.log(_step50);
          console.log(_step50.status);



          //First of all retrieve the invoice in the case
          console.log(data);
          const invoiceName = data.case.case.Title + ".pdf";
          const invoiceUrl = data.case.case.FileRef + "/" + invoiceName;
          const invoiceId = await window.get_file_id(data.appState, invoiceUrl);
          console.log(invoiceName);
          console.log(invoiceUrl);
          console.log(invoiceId);

          const docData = {
              name: invoiceName,
              template: {
                  type: "PDF",
                  title: invoiceName,
                  url: invoiceUrl,
              },
              uniqueId: invoiceId,
          }

          console.log(">>> Begin getMailPropertyOnPDF");
          const email = await window.getMailPropertyOnPDF(data.appState, docData);
          console.log(">>> email", email);
          if (email !== "") {
              const properties = data.case.case.customProperties;
              console.log(">>> properties", properties);
              const updatedProperties = properties.map(property => {
                  if (property.name === "cptaMailoutEmail") {
                      return { ...property, value: email };
                  }
                  return property;
              });
              console.log(">>> updatedProperties", updatedProperties);

              const json = { ...data.case };
              json.case.customProperties = updatedProperties;
              console.log(">>> json", json);
              await window.updateJson(data.appState, json, {
                  isNew: false,
                  tempFileName: "checklist.json",
                  fileName: "checklist.json",
                  destination: data.case.case?.FileRef,
              });
          } else {
              console.log(">>> email empty");
          }
          console.log(">>> End getMailPropertyOnPDF");

          console.log(">>> Begin addBookmarkOnPdf");
          await window.addBookmarkOnPdf(data.appState, docData);
          console.log(">>> End addBookmarkOnPdf");


          // First rename de invoice with the bookmarks
          try {
              const invoiceWbUrl = data.case.case.FileRef + "/" + data.case.case.Title + "_bookmarkAdded_bookmarkAdded.pdf";
              const file = sp.web.getFileByServerRelativeUrl(invoiceWbUrl);
              const item = await file.getItem();
              await item.update({
                  FileLeafRef: data.case.case.FileRef + "/" + data.case.case.Title + "_withBookmarks.pdf"
              });
              console.log("File renamed");
          } catch (error) {
              console.error("Error renaming file:", error);
          }

          obj.status = "Bookmarks ajoutés au PDF";
          return obj;
      } catch (err) {
          console.error(err);
          obj.status = "erreur";
          obj.error = err.message;
          return obj;
      }
  }

}
// Expose en global
window.traiterStep = traiterStep;
