import React, { useState, useRef } from "react";
import axios from "axios";

const InvoiceUploadPage = () => {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const dropRef = useRef(null);

  // Drag & Drop Handlers
  const handleDragOver = (e) => {
    e.preventDefault();
    dropRef.current.classList.add("border-blue-400");
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    dropRef.current.classList.remove("border-blue-400");
  };

  const handleDrop = (e) => {
    e.preventDefault();
    dropRef.current.classList.remove("border-blue-400");
    const droppedFile = e.dataTransfer.files[0];
    if (validateFile(droppedFile)) setFile(droppedFile);
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (validateFile(selectedFile)) setFile(selectedFile);
  };

  // Validate file type and size
  const validateFile = (f) => {
    const allowedTypes = ["application/pdf", "image/jpeg", "image/png"];
    const maxSize = 10 * 1024 * 1024; // 10 MB
    if (!allowedTypes.includes(f.type)) {
      alert("Only PDF, JPG, or PNG files are allowed.");
      return false;
    }
    if (f.size > maxSize) {
      alert("File is too large. Maximum size is 10MB.");
      return false;
    }
    return true;
  };

  const handleUpload = async () => {
    if (!file) return alert("Please select a file first!");
    setLoading(true);
    setSuccess(false);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await axios.post(
        "http://localhost:8081/api/document/upload",
        formData,
        {
          responseType: "blob",
          headers: { "Content-Type": "multipart/form-data" },
        }
      );

      // Trigger download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "invoice.xlsx");
      document.body.appendChild(link);
      link.click();
      link.remove();

      setSuccess(true);
      setFile(null);
    } catch (err) {
      console.error(err);
      alert("Failed to process invoice.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto p-6 mt-10 border rounded shadow-md">
      <h2 className="text-2xl font-bold mb-4 text-center">Upload Invoice</h2>

      <div
        ref={dropRef}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className="border-2 border-dashed border-gray-400 rounded p-10 text-center mb-4 hover:border-blue-500 transition-colors"
      >
        {file ? (
          <p className="text-gray-700">Selected file: {file.name}</p>
        ) : (
          <p className="text-gray-500">
            Drag & drop a PDF, JPG, or PNG file here <br /> or click to select
          </p>
        )}
        <input
          type="file"
          accept=".pdf,.jpg,.png"
          onChange={handleFileChange}
          className="absolute w-full h-full opacity-0 cursor-pointer top-0 left-0"
        />
      </div>

      <button
        onClick={handleUpload}
        disabled={loading}
        className={`w-full px-4 py-2 text-white rounded ${
          loading ? "bg-gray-400" : "bg-blue-500 hover:bg-blue-600"
        }`}
      >
        {loading ? (
          <span className="flex items-center justify-center">
            <svg
              className="animate-spin h-5 w-5 mr-2 text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              ></circle>
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8v8z"
              ></path>
            </svg>
            Processing...
          </span>
        ) : (
          "Upload & Download Excel"
        )}
      </button>

      {success && (
        <p className="mt-4 text-green-600 font-medium text-center">
          Excel file downloaded successfully!
        </p>
      )}
    </div>
  );
};

export default InvoiceUploadPage;
