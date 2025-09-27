document.addEventListener('DOMContentLoaded', function() {
    const uploadForm = document.getElementById('uploadForm');
    const fileInput = document.getElementById('audioFile');
    const fileName = document.getElementById('fileName');
    const submitBtn = document.querySelector('.submit-btn');
    const loading = document.getElementById('loading');
    const result = document.getElementById('result');
    const error = document.getElementById('error');
    const newTranscriptionBtn = document.getElementById('newTranscription');
    const retryBtn = document.getElementById('retryBtn');

    // File selection handler
    fileInput.addEventListener('change', function() {
        if (this.files.length > 0) {
            const file = this.files[0];
            fileName.textContent = `Archivo seleccionado: ${file.name} (${formatFileSize(file.size)})`;
            submitBtn.disabled = false;
            
            // Validate file type
            if (!file.name.toLowerCase().endsWith('.wav')) {
                showError('Por favor, selecciona un archivo WAV válido.');
                submitBtn.disabled = true;
                return;
            }
            
            // Validate file size (max 10MB)
            if (file.size > 10 * 1024 * 1024) {
                showError('El archivo es demasiado grande. Máximo permitido: 10MB.');
                submitBtn.disabled = true;
                return;
            }
        } else {
            fileName.textContent = '';
            submitBtn.disabled = true;
        }
    });

    // Form submission handler
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        if (!fileInput.files.length) {
            showError('Por favor, selecciona un archivo WAV.');
            return;
        }
        
        const file = fileInput.files[0];
        const formData = new FormData();
        formData.append('file', file);
        
        // Show loading state
        showLoading();
        
        // Make API call
        fetch('/api/audio/transcribe', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            hideLoading();
            
            if (data.error) {
                showError(data.error);
            } else {
                showResult(data);
            }
        })
        .catch(err => {
            hideLoading();
            showError('Error de conexión: ' + err.message);
        });
    });

    // New transcription button
    newTranscriptionBtn.addEventListener('click', function() {
        resetForm();
    });

    // Retry button
    retryBtn.addEventListener('click', function() {
        hideError();
    });

    // Utility functions
    function showLoading() {
        hideAllSections();
        loading.classList.remove('hidden');
    }

    function hideLoading() {
        loading.classList.add('hidden');
    }

    function showResult(data) {
        hideAllSections();
        
        document.getElementById('objectId').textContent = data.id;
        document.getElementById('audioSize').textContent = formatNumber(data.audioSize);
        document.getElementById('transcribedText').textContent = data.transcribedText || 'No se pudo obtener transcripción del audio.';
        
        result.classList.remove('hidden');
    }

    function showError(message) {
        hideAllSections();
        document.getElementById('errorMessage').textContent = message;
        error.classList.remove('hidden');
    }

    function hideError() {
        error.classList.add('hidden');
        document.querySelector('.upload-section').style.display = 'block';
    }

    function hideAllSections() {
        loading.classList.add('hidden');
        result.classList.add('hidden');
        error.classList.add('hidden');
        document.querySelector('.upload-section').style.display = 'none';
    }

    function resetForm() {
        uploadForm.reset();
        fileName.textContent = '';
        submitBtn.disabled = true;
        hideAllSections();
        document.querySelector('.upload-section').style.display = 'block';
    }

    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    function formatNumber(num) {
        return new Intl.NumberFormat('es-ES').format(num);
    }
});