"use client"
import React from 'react'
import { updateAssetRequestById } from '../action/AssetRequestAction';
import { useForm } from 'react-hook-form';
import { useSession } from 'next-auth/react';

export default function UpdateAssetRequest({ onClose, record, onUpdate }) {
    const { data: session } = useSession();
    const token = session?.accessToken;

    const { reset, register, handleSubmit, formState: { errors } } = useForm();

    const handleAssetRequestUpdate = async (formData) => {
        try {
            const updatePayload = {
                assetName: formData?.assetName,
                qty: parseInt(formData.qty, 10), 
                unit: parseInt(formData.unit, 10),
                reason: formData?.reason,
                createdAt: "2024-11-20T02:21:33.989Z", 
                attachment: "static-attachment-url-or-string",
            };
            console.log("jfsjfsdjf")
            console.log("token", token)
            console.log("payload", updatePayload)
            const updateRequest = await updateAssetRequestById(token, updatePayload, record.requestId);
            console.log("update requ", updateRequest)
            reset();
            onUpdate?.();
            onClose?.();
        } catch (error) {
            console.error("Error updating department:", error);
        }
    };
    return (
        <>
            {/* overlay */}
            <div className="fixed inset-0 bg-black bg-opacity-50 z-40" onClick={onClose}></div>
            {/* center */}
            <div id="popup-modal" tabIndex="-1" className="fixed inset-0 flex items-center justify-center z-50">
                <div className="relative p-4 w-[700px] h-[95%]">
                    <form onSubmit={handleSubmit(handleAssetRequestUpdate)} className="relative bg-white rounded-lg shadow  h-full flex flex-col">
                        <button onClick={onClose} type="button" className="absolute top-3 right-2.5 text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm w-8 h-8 ms-auto inline-flex justify-center items-center dark:hover:bg-gray-600 dark:hover:text-white">
                            <svg className="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 14 14">
                                <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M1 1l6 6m0 0 6 6M7 7l6-6M7 7L1 13" />
                            </svg>
                            <span className="sr-only">Close modal</span>
                        </button>
                        <div className="p-4 md:p-5 overflow-y-auto flex-grow custom-scroll">
                            <div className="flex gap-3">
                                <div>
                                    <span className="font-semibold text-lg leading-loose">Update Asset Request</span>
                                    <h3 className="mb-5 text-sm font-normal text-gray-500 dark:text-gray-400">You can feel free to update wherever you need regarding asset requests.</h3>
                                </div>
                            </div>
                            <hr className='mb-5' />
                            <div className='mb-3'>
                                <label htmlFor="first_name" className="block mb-2 text-sm font-medium  text-[#344054]">Asset Name</label>
                                <input {...register('assetName', { required: true })} type="text" id="first_name" className="bg-[#F8FAFC] text-gray-900 text-sm rounded-lg block w-full p-2.5  placeholder:text-[#273240] dark:text-white" placeholder="Computer" required defaultValue={record.assetName || "N/A"} />
                            </div>
                            <div className='mb-3'>
                                <label htmlFor="first_name" className="block mb-2 text-sm font-medium  text-[#344054]">Qty</label>
                                <input {...register('qty', { required: true })} type="text" id="first_name" className="bg-[#F8FAFC] text-gray-900 text-sm rounded-lg block w-full p-2.5  placeholder:text-[#273240] dark:text-white" placeholder="10 Unit" required defaultValue={record.qty || "N/A"} />
                            </div>
                            <div className='mb-3'>
                                <label htmlFor="first_name" className="block mb-2 text-sm font-medium  text-[#344054]">Unit</label>
                                <input {...register('unit', { required: true })} type="text" id="first_name" className="bg-[#F8FAFC] text-gray-900 text-sm rounded-lg block w-full p-2.5  placeholder:text-[#273240] dark:text-white" placeholder="10 Unit" required defaultValue={record.unit || "N/A"} />
                            </div>
                            <div className='mb-3'>
                                <label htmlFor="message" className="block mb-2 text-sm font-medium text-[#344054]">Reason</label>
                                <textarea {...register('reason', { required: true })} id="message" rows="4" className="block p-2.5 w-full text-sm text-gray-900 bg-[#F8FAFC] rounded-lg  dark:border-gray-600 placeholder:text-[#273240] dark:text-white" placeholder="I cannot turn on my computer it always blue screen while turn it on" defaultValue={record.reason || "N/A"}></textarea>
                            </div>
                            <div className='mb-3'>
                                <label htmlFor="message" className="block mb-2 text-sm font-medium text-[#344054]">Description</label>
                                <input type="hidden" {...register('createdAt')} value="2024-11-20T02:21:33.989Z" />
                                {/* <textarea {...register('createdAt', { required: true })} id="message" rows="4" className="block p-2.5 w-full text-sm text-gray-900 bg-[#F8FAFC] rounded-lg  dark:border-gray-600 placeholder:text-[#273240] dark:text-white" placeholder="I cannot turn on my computer it always blue screen while turn it on"></textarea> */}
                            </div>
                            <div className="mb-10">
                                <label htmlFor="message" className="block mb-2 text-sm font-medium text-[#344054]">Attachment</label>
                                {/* <img src={record.attachment || "https://images.unsplash.com/photo-1553427054-3cf4c0712aa9?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8Ymx1ZXNjcmVlbnxlbnwwfHwwfHx8MA%3D%3D"} alt="" className="rounded-md w-full" /> */}
                                <input type="hidden" {...register('attachment')} value="static-attachment-url-or-string" />
                            </div>
                        </div>
                        <div className="flex justify-end p-4">
                            <button
                                onClick={onClose}
                                type="button"
                                className="w-[150px] text-[#344054] bg-white font-semibold border border-gray-200 rounded-lg text-sm inline-flex items-center px-12 py-2.5"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="w-[150px] ms-3 text-sm font-semibold text-white focus:outline-none bg-[#4b68ff] rounded-lg border-none"
                            >
                                Save
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            <style jsx>{`
                .custom-scroll::-webkit-scrollbar {
                    width: 8px;
                }
                .custom-scroll::-webkit-scrollbar-track {
                    background: #f1f1f1;
                }
                .custom-scroll::-webkit-scrollbar-thumb {
                    background-color: #888;
                    border-radius: 10px;
                    border: 2px solid #f1f1f1;
                }
                .custom-scroll::-webkit-scrollbar-thumb:hover {
                    background-color: #555;
                }
            `}</style>
        </>
    )
}
