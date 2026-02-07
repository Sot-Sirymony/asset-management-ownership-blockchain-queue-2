// utils/resourceLabel.js
// Object mapping resource names to user-friendly labels.
export const resourceLabels = {
    categories: "Category",
    users: "User",
    dashboard: "Dashboard",
    asset: "Asset Request",
    "report-issue": "Report Issue",
    history: "History",
    department: "Department",
  };
  
  // Function to get label for a given resource name
  export const getResourceLabel = (resourceName) => {
    return resourceLabels[resourceName] || resourceName.charAt(0).toUpperCase() + resourceName.slice(1);
  };

  export const getResourceLabels = () => resourceLabels;
  