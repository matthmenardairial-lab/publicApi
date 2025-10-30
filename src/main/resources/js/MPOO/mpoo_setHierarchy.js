async function traiterStep(data) {
    const obj = {};
    console.log(">>> Begin mpoo_setHierarchy execution");
    const updatedSteps = [...data.case.steps];

    const user = await window.getCurrentUser(data.appState);
    console.log("user", user);

    /**
     * allHierarchy is a list of IUserDto elements as described below
     * @typedef {Object} IUserDto
     * @property {string} id
     * @property {string} displayName
     * @property {string} mail
     * @property {string} jobTitle
     * @property {string} userPrincipalName
     */
    const allHierarchy = await window.getHierarchy(data.appState, user);
    console.log("👔 Hiérarchie reçue :", allHierarchy);

    if (allHierarchy.length > 0) {
        // Manage the service's chief on step 510
        if (allHierarchy[0] !== null && allHierarchy[0] !== "") {
            const servicesChief = allHierarchy[0];
            console.log(">>> servicesChief: ", servicesChief);

            const item510 = updatedSteps.findIndex((_step) => _step.id === 510);
            const _step510 = updatedSteps[item510];
            console.log(">>> _step510: ", _step510);

            const _assignTo = [];
            const newAssigned = {};
            newAssigned.email = "violeta.popescu@inetum-realdolmen.world";
            newAssigned.displayName = "Violeta Popescu";
            newAssigned.loginName = "violeta.popescu_inetum-realdolmen.world#ext#@mnx4.onmicrosoft.com";

            _assignTo.push(newAssigned);
            _step510.assignedTo = _assignTo;
            updatedSteps[item510] = { ...updatedSteps[item510], ..._step510 };
        }

        // Manage the department's chief on step 520
        if (allHierarchy[1] !== null && allHierarchy[1] !== "") {
            const departmentChief = allHierarchy[1];
            console.log(">>> departmentChief: ", departmentChief);

            const item520 = updatedSteps.findIndex((_step) => _step.id === 520);
            const _step520 = updatedSteps[item520];
            console.log(">>> _step520: ", _step520);

            const _assignTo = [];
            const newAssigned = {};
            newAssigned.email = departmentChief.mail;
            newAssigned.displayName = departmentChief.displayName;
            newAssigned.loginName = departmentChief.userPrincipalName;

            _assignTo.push(newAssigned);
            _step520.assignedTo = _assignTo;
            updatedSteps[item520] = { ...updatedSteps[item520], ..._step520 };
        }

        // Manage the subDiv's chief on step 530
        if (allHierarchy[2] !== null && allHierarchy[2] !== "") {
            const subDivChief = allHierarchy[2];
            console.log(">>> subDivChief: ", subDivChief);

            const item530 = updatedSteps.findIndex((_step) => _step.id === 530);
            const _step530 = updatedSteps[item530];
            console.log(">>> _step530: ", _step530);

            const _assignTo = [];
            const newAssigned = {};
            newAssigned.email = subDivChief.mail;
            newAssigned.displayName = subDivChief.displayName;
            newAssigned.loginName = subDivChief.userPrincipalName;

            _assignTo.push(newAssigned);
            _step530.assignedTo = _assignTo;
            updatedSteps[item530] = { ...updatedSteps[item530], ..._step530 };
        }

        const json = { ...data.case };
        json.steps = updatedSteps;
        console.log(">>> Json: ", json);

        await window.updateJson(data.appState, json, {
            isNew: false,
            tempFileName: "checklist.json",
            fileName: "checklist.json",
            destination: data.case.case?.FileRef,
        });
        obj.status = "Steps modified";
        return obj;
    }


}
// Expose en global
window.traiterStep = traiterStep;
