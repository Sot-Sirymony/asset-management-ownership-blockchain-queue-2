"use client";

import { stringify } from "query-string";
import restDataProvider from "@refinedev/simple-rest";

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/api/v1/admin`;

// Helper function to generate filter query string for search
const generateFilter = (filters) => {
    if (!filters) return {};

    return filters.reduce((acc, filter) => {
        if (filter.field === "dep_name" && filter.operator === "contains") {
            acc.search = filter.value;
        }
        return acc;
    }, {});
};

export const dataProvider = (axios) => {
    const baseProvider = restDataProvider(API_URL, axios);

    return {
        ...baseProvider,
        getList: async ({ resource, filters }) => {
            try {
                const url = `${API_URL}/${resource}`;

                const query = {
                    ...(filters && { ...generateFilter(filters) }),
                };

                console.log("Fetching data with query:", query); // Log query parameters

                const { data } = await axios.get(`${url}?${stringify(query)}`);

                console.log("Fetched data:", data); // Log the fetched data

                return {
                    data: data.departments || [], // All departments fetched from backend
                    total: data.departments?.length || 0, // Total count for frontend pagination
                };
            } catch (error) {
                console.error("Error fetching departments:", error);
                throw error;
            }
        },
    }
};

export const departmentDataProvider = dataProvider;