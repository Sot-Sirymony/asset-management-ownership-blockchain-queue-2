"use client";

import { UploadOutlined } from "@ant-design/icons";
import { Create, useForm } from "@refinedev/antd";
import { Form, Input, Typography, Modal, Button, Select } from "antd";
import { useParams } from "next/navigation";
import { useState, useEffect } from "react";
import { useSession } from "next-auth/react";
import { getAsset, transferAsset } from "../../../../components/service/asset.service";
import { formatDateBC } from "../../../../utils/formatDate";
import { getUserById } from "../../../../components/action/UserAction";
import Loading from "../../../../components/components/Loading";
import { getAllUser } from "../../../../components/service/user.service";

const { Title } = Typography;


export default function CategoryShow() {
    const { formProps, form } = useForm();
    const { id } = useParams();
    const { data: session } = useSession();
    const token = session?.accessToken;
    const role = session?.user?.role;
    const [loading, setLoading] = useState(false)
    const [previewVisible, setPreviewVisible] = useState(false);
    const [previewImage, setPreviewImage] = useState("");
    const [asset, setAsset] = useState(null)

    // Transfer ownership (Admin)
    const [transferOpen, setTransferOpen] = useState(false);
    const [users, setUsers] = useState([]);
    const [transferLoading, setTransferLoading] = useState(false);
    const [newAssignTo, setNewAssignTo] = useState(null);

    

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true)
            try {
                const getUser = await getAsset(token, id);
                setAsset(getUser);
    
                form.setFieldsValue({
                    assetName: getUser.asset_name || "",
                    quantity: getUser.qty || 0,
                    unit: getUser.unit || "",
                    
                    condition: getUser.condition || "",
                    assignedDate: formatDateBC(getUser.created_at) || "",
                    attachment: getUser.attachment || "null",
                });
    
                if (getUser.assign_to) {
                    const fetchedUser = await getUserById(token, getUser.assign_to);
                    form.setFieldsValue({
                        assignTo: fetchedUser.fullName || "Unassigned",
                    })
                }
            } catch (error) {
                console.error("Error fetching data:", error);
            }   finally {
                setLoading(false)
              }
        };
    
        fetchData();
    }, [token, id]);

    // Load user list when transfer modal opens
    useEffect(() => {
        const loadUsers = async () => {
            if (!token || !transferOpen) return;
            try {
                // fetch a larger page so you can pick users easily
                const all = await getAllUser(token, 100, 1);
                setUsers(Array.isArray(all) ? all : []);
            } catch (e) {
                console.error("Failed to load users:", e);
                setUsers([]);
            }
        };
        loadUsers();
    }, [token, transferOpen]);

    const submitTransfer = async () => {
        if (!newAssignTo) return;
        setTransferLoading(true);
        try {
            const ok = await transferAsset(token, { newAssignTo }, id);
            if (ok) {
                // refresh asset to show new owner
                const updated = await getAsset(token, id);
                setAsset(updated);
                if (updated?.assign_to) {
                    const fetchedUser = await getUserById(token, updated.assign_to);
                    form.setFieldsValue({ assignTo: fetchedUser?.fullName || "Unassigned" });
                }
                setTransferOpen(false);
                setNewAssignTo(null);
            }
        } catch (e) {
            console.error("Transfer failed:", e);
        } finally {
            setTransferLoading(false);
        }
    };


    const handlePreview = () => {
        setPreviewImage(asset?.attachment);
        setPreviewVisible(true);
    };

    return (
        <section className={"mx-[20px]"}>
            {loading ? (
          <Loading />
        ) : (
            <Form {...formProps} form={form} layout="vertical">
                <Create title={""}>
                    <div className="leading-loose">
                        <div className="flex items-center justify-between gap-4">
                            <div>
                                <Title level={2} className="text-[18px] text-[#151D48] font-semibold">View Asset</Title>
                                <p className="text-[#6F6C90]">View detailed asset information</p>
                            </div>

                            {role === "ADMIN" && (
                                <Button type="primary" onClick={() => setTransferOpen(true)}>
                                    Transfer Ownership
                                </Button>
                            )}
                        </div>
                    </div>
                    <hr className="border-[#3D7EDF] opacity-[30%]" />
                    <div className="flex gap-10 mt-5">
                        <div className="flex-1">
                            <Form.Item label={<span style={{ color: "#344054" }}>Asset Name</span>} name="assetName">
                                <Input readOnly style={{ color: "#273240" }} />
                            </Form.Item>

                            <Form.Item label={<span style={{ color: "#344054" }}>Qty</span>} name="quantity">
                                <Input readOnly type="number" style={{ color: "#273240" }} />
                            </Form.Item>

                            <Form.Item label={<span style={{ color: "#344054" }}>Unit</span>} name="unit">
                                <Input readOnly style={{ color: "#273240" }} />
                            </Form.Item>

                            <Form.Item label={<span style={{ color: "#344054" }}>Assign To</span>} name="assignTo">
                                <Input readOnly style={{ color: "#273240" }} />
                            </Form.Item>

                            <Form.Item label={<span style={{ color: "#344054" }}>Condition</span>} name="condition">
                                <Input readOnly style={{ color: "#273240" }} />
                            </Form.Item>

                            <Form.Item label={<span style={{ color: "#344054" }}>Assigned Date</span>} name="assignedDate">
                                <Input readOnly style={{ color: "#273240" }} />
                            </Form.Item>
                        </div>

                        <div className="flex-1">
                            {/* <Form.Item label={<span style={{ color: "#344054" }}>Description</span>} name="description">
                                <Input.TextArea readOnly rows={5} style={{ color: "#273240" }} />
                            </Form.Item> */}

                            <Form.Item label={<span style={{ color: "#344054" }}>Attachment</span>} name="attachment">
                                {asset?.attachment ? (
                                    <div
                                        onClick={handlePreview}
                                        style={{
                                            cursor: "pointer",
                                            display: "flex",
                                            justifyContent: "center",
                                            alignItems: "center",
                                        }}
                                    >
                                        <img
                                            src={asset.attachment}
                                            alt="Attachment"
                                            style={{ maxWidth: "100%", maxHeight: "200px" }}
                                        />
                                    </div>
                                ) : (
                                    <div className="ant-upload-drag-icon">
                                        <UploadOutlined />
                                        <p>No attachment available</p>
                                    </div>
                                )}
                            </Form.Item>
                        </div>
                    </div>
                </Create>

                <Modal
                    open={previewVisible}
                    title="Image Preview"
                    footer={null}
                    onCancel={() => setPreviewVisible(false)}
                    width={900}
                >
                    <img alt="Preview" style={{
                        width: "100%",
                        maxWidth: "800px",
                        height: "auto",
                        margin: "0 auto",
                        display: "block",
                        objectFit: "contain"
                    }} src={previewImage} />
                </Modal>


                <Modal
                    open={transferOpen}
                    title="Transfer Ownership"
                    okText="Transfer"
                    confirmLoading={transferLoading}
                    onOk={submitTransfer}
                    onCancel={() => {
                        setTransferOpen(false);
                        setNewAssignTo(null);
                    }}
                >
                    <p className="mb-2">Select the new owner for this asset.</p>
                    <Select
                        className="w-full"
                        placeholder="Select user"
                        value={newAssignTo}
                        onChange={(v) => setNewAssignTo(v)}
                        options={users
                            .filter((u) => u?.userId)
                            .map((u) => ({
                                value: Number(u.userId),
                                label: `${u.fullName || u.username || "User"} (ID: ${u.userId})`,
                            }))}
                        showSearch
                        optionFilterProp="label"
                    />
                </Modal>
            </Form>
        )}
        </section>
    );
}
