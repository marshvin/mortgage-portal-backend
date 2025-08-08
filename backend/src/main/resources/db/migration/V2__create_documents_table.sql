-- Create documents table
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    presigned_url VARCHAR(1000) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_documents_application 
        FOREIGN KEY (application_id) 
        REFERENCES applications(id) 
        ON DELETE CASCADE,
    
    -- Additional constraints
    CONSTRAINT chk_file_name_not_empty CHECK (file_name != ''),
    CONSTRAINT chk_file_type_not_empty CHECK (file_type != ''),
    CONSTRAINT chk_presigned_url_not_empty CHECK (presigned_url != '')
);

-- Create indexes for performance
CREATE INDEX idx_documents_application_id ON documents(application_id);
CREATE INDEX idx_documents_uploaded_at ON documents(uploaded_at);
CREATE INDEX idx_documents_file_type ON documents(file_type);

-- Add comment to table
COMMENT ON TABLE documents IS 'Stores document metadata for mortgage applications';
COMMENT ON COLUMN documents.id IS 'Unique identifier for the document';
COMMENT ON COLUMN documents.application_id IS 'Reference to the mortgage application';
COMMENT ON COLUMN documents.file_name IS 'Original name of the uploaded file';
COMMENT ON COLUMN documents.file_type IS 'File extension/type (e.g., pdf, jpeg)';
COMMENT ON COLUMN documents.file_size IS 'File size in bytes';
COMMENT ON COLUMN documents.presigned_url IS 'S3 presigned URL for file access';
COMMENT ON COLUMN documents.uploaded_at IS 'Timestamp when document was uploaded';
