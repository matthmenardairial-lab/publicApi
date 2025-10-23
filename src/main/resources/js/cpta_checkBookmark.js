async function traiterStep(data) {
  const obj = {};
  try {
      console.log(">>> Begenning of the script cpta_checkBookmark");
      console.log(">>> data", data);
      const updatedSteps = [...data.case.steps];
      console.log(">>> updatedSteps",updatedSteps);
      const item100 = updatedSteps.findIndex((_step) => _step.id === 100);
      const _step100 = updatedSteps[item100];
      console.log(">>> _step100", _step100);

      const invoiceName = data.case.case.Title + "_withBookmark.pdf";
      const invoiceUrl = data.case.case.FileRef + "/" + invoiceName;
      const invoiceId = await window.get_file_id(data.appState, invoiceUrl);
      console.log(">>> invoiceName", invoiceName);
      console.log(">>> invoiceUrl", invoiceUrl);
      console.log(">>> invoiceId", invoiceId);

      const docData = {
          name: invoiceName,
          template: {
              type: "PDF",
              title: invoiceName,
              url: invoiceUrl,
          },
          uniqueId: invoiceId,
      }
        console.log(">>> Begin detectBookmarkOnInvoice");
        const arePresent = await window.detectBookmarkOnInvoice(data.appState, docData);
        console.log(">>> End detectBookmarkOnInvoice");
        console.log(">>> arePresent: ", arePresent);

      let commentContent = "All Bookmarks are present";
      if (!arePresent) {
          console.log(">>> Passe dans le if du !arePresent");
          commentContent = "One or more bookmark is missing";
      }
      const createdComment = {
          "date": new Date().toDateString(),
          "name" : "Mathieu Menard",
          "email": "Mathieu.Menard@inetum-realdolmen.world",
          "text": commentContent,
      }

      console.log(">>> _step100.comments", _step100.comments);
      if (_step100.comments !== undefined) {
          console.log("Passe dans le if du _step100.comments !== undefined");
          const _step100_Comments = _step100.comments;
          console.log(">>> _step100_Comments", _step100_Comments);
          const _step100_CommentsModified = _step100_Comments.push(createdComment);
          console.log(">>> _step100_CommentsModified", _step100_CommentsModified);
          _step100.comments = _step100_CommentsModified;
      } else {
          console.log("Passe dans le else du _step100.comments !== undefined");
          //_step100.comments = [createdComment];
          const _step100_Comments = [];
          _step100_Comments.push(createdComment);
          console.log(">>> _step100_Comments", _step100_Comments);
          _step100.comments = _step100_Comments;
      }
      updatedSteps[item100] = { ...updatedSteps[item100], ..._step100 };

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
