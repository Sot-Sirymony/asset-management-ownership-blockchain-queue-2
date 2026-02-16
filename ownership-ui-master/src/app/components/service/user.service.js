import { reqHeader } from "../../utils/header.config";
import Toastify from 'toastify-js';
import "toastify-js/src/toastify.css";


export const getAllUser = async (token, size = 20, page = 1) => {
    const header = await reqHeader(token);

    try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/admin/getAllUser?size=${size}&page=${page}`, {
            headers: header,
            cache: "no-store",
            next: { tag: ["getAllUser"] },
        });

        if (!res.ok) {
            console.error(`API error: ${res.status} - ${res.statusText}`);
            return [];
        }

        const text = await res.text();
        if (!text) {
            console.error("Empty response from API");
            return [];
        }

        if (res.headers.get('Content-Type')?.includes('application/json')) {
            const data = JSON.parse(text);
            const users = data.payload || [];
            if (users.length > 0) {
                return users;
            }

            // Fallback: when no non-admin users exist yet, include current profile
            // so assignment/transfer forms still have at least one selectable user.
            const profileRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/getProfile`, {
                headers: header,
                cache: "no-store",
            });
            if (profileRes.ok && profileRes.headers.get("Content-Type")?.includes("application/json")) {
                const profileData = await profileRes.json();
                const profile = profileData?.payload;
                if (profile?.userId) {
                    return [{
                        userId: profile.userId,
                        fullName: profile.fullName || profile.username || "Current User",
                        profileImg: profile.profileImg || "",
                        department: profile.department || { dep_name: "" },
                    }];
                }
            }

            return [];
        } else {
            console.error("Invalid Content-Type:", res.headers.get('Content-Type'));
            return [];
        }
    } catch (error) {
        console.error("Fetch error:", error);
        return [];
    }
};


export const getUser = async(token,id) => {
    const header = await reqHeader(token);
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/admin/getUser/${id}`, {
        headers: header,
        next: { tag: ["getAllUser"] },
    });
    console.log("get user",res)
    const {payload} = await res.json();
    console.log("payload",payload)
    return payload;
}

export const getProfile = async(token) => {
    const header = await reqHeader(token);
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/v1/getProfile`, {
        headers: header,
        next: { tag: ["getProfile"] },
    });
    const {payload} = await res.json();
    return payload;
}

export const updateUserProfile = async(token,data, user_id) => {
    const header = await reqHeader(token);
    const updateInfo = {
        full_name: data?.full_name,
        gender: data?.gender,
        phone_number: data?.phone_number,
        address: data?.address,
        dob: data?.dob,
        place_of_birth: data?.place_of_birth,
        description: data?.description,
        profile_img: data?.profile_img
    };
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/admin/updateUser/${user_id}`,
        {
            method: "PUT",
            headers: header,
            cache: "no-store",
            body: JSON.stringify(updateInfo),
        },
        {
            next: {
                tag: ["updateUserProfile"],
            },
        }
    );
    console.log("http", res);
    if(res.status === 200){
        Toastify({
            text: "Update user success!!!",
            className: "success-toast",
        }).showToast();
    }
    const payload = await res.json();
    return payload;
}

export const updateProfile = async(token,data) => {
    const header = await reqHeader(token);
    const updateInfo = {
        full_name: data?.full_name,
        gender: data?.gender,
        phone_number: data?.phone_number,
        address: data?.address,
        dob: data?.dob,
        place_of_birth: data?.place_of_birth,
        description: data?.description,
        profile_img: data?.profile_img
    };
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/updateProfile`,
        {
            method: "PUT",
            headers: header,
            cache: "no-store",
            body: JSON.stringify(updateInfo),
        },
        {
            next: {
                tag: ["updateProfile"],
            },
        }
    );
    console.log("raw",res)
    const payload = await res.json();
    console.log("payload",payload)
    if(payload.payload == true){
        Toastify({
            text: "Update profile successfully!!!",
            className: "success-toast",
        }).showToast();
    }
    return payload;
}


export const updatePassword = async(token, data) => {
    const header = await reqHeader(token);
    const updatepassword = {
        oldPassword: data?.oldPassword,
        newPassword: data?.newPassword,
        confirmPassword: data?.confirmPassword,
    };
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/changePassword`,
        { 
            method: "PUT",
            headers: header,
            body: JSON.stringify(updatepassword),
        },
        {
            next: {
                tag: ["updatePassword"],
            },
        }
    );
    const payload = await res.json();
    return payload;
}


export const deleteUser = async(token, user_id) => {
    const header = await reqHeader(token);
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/admin/deleteUser/${user_id}`,
        {
            method: "DELETE",
            headers: header,
        },
        {
            next: {
                tag: ["deleteUser"],
            },
        }
    );
    console.log("Res",res);
    const { payload } = await res.json();
    if (payload === true) {
        Toastify({
            text: "Delete user success!!!",
            className: "success-toast",
        }).showToast();
    }else{
        Toastify({
            text: "You are admin can't delete yourself!!!",
            className: "error-toast",
        }).showToast(); 
    }
    return payload;
}

export const createUser = async (token, data) => {
    const header = await reqHeader(token);
    const {fullName, username, password, department, email} = data
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/v1/admin/register_user`,
        {
            method: "POST",
            headers: header,
            body: JSON.stringify({
                fullName,
                username,
                password,
                department,
                email,
            }),
        },
        {
            next: { tag: ["createUser"] },
        }
    );
    console.log("Raw response", res)

    const response = await res.json();

    console.log("payload", response)

    if(response.password == "Password must be at least 8 characters long and include both letters and numbers"){
        console.log("fuck")
        Toastify({
            text: "Please input a password that is at least 8 characters long and includes both letters and numbers",
            className: "error-toast",
        }).showToast();
    }

    if (!res.ok) {
        if (response && response.detail && response.detail.includes("is already registered")) {
            Toastify({
                text: "This user is already registered",
                className: "error-toast",
            }).showToast();
        } else if (response && response.detail) {
            Toastify({
                text: response.detail,  
                className: "error-toast",
            }).showToast();
        }
    } else {
        Toastify({
            text: "User created successfully!",
            className: "success-toast",
        }).showToast();
    }
    

    return response.payload;
}