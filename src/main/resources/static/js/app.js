document.addEventListener('DOMContentLoaded', function() {
    M.AutoInit();
    
    const getSampleBtn = document.getElementById('getSampleBtn');
    const processDataBtn = document.getElementById('processDataBtn');
    const continueProcessingBtn = document.getElementById('continueProcessingBtn');
    const continueProcessingLabel = document.getElementById('continueProcessingLabel');
    const sampleRecordsSection = document.getElementById('sampleRecordsSection');
    const processingResultSection = document.getElementById('processingResultSection');
    const sourceRecordsJson = document.getElementById('sourceRecordsJson');
    const processedRecordsJson = document.getElementById('processedRecordsJson');
    const verificationRecordJson = document.getElementById('verificationRecordJson');
    const sampleResultMessage = document.getElementById('sampleResultMessage');
    const processingResultMessage = document.getElementById('processingResultMessage');
    const loadingOverlay = document.getElementById('loadingOverlay');
    const totalProcessed = document.getElementById('totalProcessed');
    const totalMatched = document.getElementById('totalMatched');
    const lastTimestamp = document.getElementById('lastTimestamp');
    
    const sourceIndex = document.getElementById('sourceIndex');
    const targetIndex = document.getElementById('targetIndex');
    const filterField = document.getElementById('filterField');
    const filterValue = document.getElementById('filterValue');
    const inputJsonField = document.getElementById('inputJsonField');
    const masterJsonPath = document.getElementById('masterJsonPath');
    const timestampField = document.getElementById('timestampField');
    const batchSize = document.getElementById('batchSize');
    
    getSampleBtn.addEventListener('click', getSampleRecords);
    processDataBtn.addEventListener('click', processData);
    continueProcessingBtn.addEventListener('click', continueProcessing);
    
    function getSampleRecords() {
        if (!validateForm()) return;
        
        showLoading();
        
        const requestData = getRequestData();
        requestData.batchSize = 10; // Limit sample size
        
        fetch('/api/elastic/sample', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to retrieve sample records');
            }
            return response.json();
        })
        .then(data => {
            displaySampleRecords(data);
            hideLoading();
        })
        .catch(error => {
            displayError(sampleResultMessage, error.message);
            hideLoading();
        });
    }
    
    function processData() {
        if (!validateForm()) return;
        
        showLoading();
        
        const requestData = getRequestData();
        
        fetch('/api/elastic/process', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to process data');
            }
            return response.json();
        })
        .then(data => {
            displayProcessingResults(data);
            hideLoading();
        })
        .catch(error => {
            displayError(processingResultMessage, error.message);
            hideLoading();
        });
    }
    
    function continueProcessing() {
        if (!validateForm()) return;
        
        showLoading();
        
        const requestData = getRequestData();
        
        fetch('/api/elastic/process', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to process data');
            }
            return response.json();
        })
        .then(data => {
            displayProcessingResults(data);
            hideLoading();
        })
        .catch(error => {
            displayError(processingResultMessage, error.message);
            hideLoading();
        });
    }
    
    function getRequestData() {
        return {
            sourceIndex: sourceIndex.value.trim(),
            targetIndex: targetIndex.value.trim(),
            filterField: filterField.value.trim() || null,
            filterValue: filterValue.value.trim() || null,
            inputJsonField: inputJsonField.value.trim() || null,
            masterJsonPath: masterJsonPath.value.trim() || null,
            timestampField: timestampField.value.trim() || null,
            lastTimestamp: lastTimestamp.textContent !== '-' ? lastTimestamp.textContent : null,
            batchSize: parseInt(batchSize.value) || 100
        };
    }
    
    function validateForm() {
        if (!sourceIndex.value.trim()) {
            displayError(sampleResultMessage, 'Source index name is required');
            return false;
        }
        
        if (!targetIndex.value.trim()) {
            displayError(sampleResultMessage, 'Target index name is required');
            return false;
        }
        
        return true;
    }
    
    function displaySampleRecords(data) {
        sampleRecordsSection.style.display = 'block';
        
        if (data.sampleSourceRecords && data.sampleSourceRecords.length > 0) {
            sourceRecordsJson.textContent = JSON.stringify(data.sampleSourceRecords, null, 2);
        } else {
            sourceRecordsJson.textContent = 'No source records found';
        }
        
        if (data.sampleProcessedRecords && data.sampleProcessedRecords.length > 0) {
            processedRecordsJson.textContent = JSON.stringify(data.sampleProcessedRecords, null, 2);
        } else {
            processedRecordsJson.textContent = 'No processed records found';
        }
        
        if (data.message) {
            displayMessage(sampleResultMessage, data.message);
        }
    }
    
    function displayProcessingResults(data) {
        processingResultSection.style.display = 'block';
        
        if (data.verificationRecord) {
            verificationRecordJson.textContent = JSON.stringify(data.verificationRecord, null, 2);
        } else {
            verificationRecordJson.textContent = 'No verification record available';
        }
        
        totalProcessed.textContent = data.totalProcessed || 0;
        totalMatched.textContent = data.totalMatched || 0;
        
        if (data.lastProcessedTimestamp) {
            lastTimestamp.textContent = data.lastProcessedTimestamp;
        }
        
        if (data.message) {
            displayMessage(processingResultMessage, data.message);
        }
        
        if (data.hasMoreRecords) {
            continueProcessingBtn.style.display = 'inline-block';
            continueProcessingLabel.style.display = 'inline-block';
        } else {
            continueProcessingBtn.style.display = 'none';
            continueProcessingLabel.style.display = 'none';
        }
    }
    
    function displayMessage(element, message) {
        element.textContent = message;
        element.className = 'message-box info';
    }
    
    function displayError(element, message) {
        element.textContent = message;
        element.className = 'message-box error';
    }
    
    function showLoading() {
        loadingOverlay.style.display = 'flex';
    }
    
    function hideLoading() {
        loadingOverlay.style.display = 'none';
    }
});
