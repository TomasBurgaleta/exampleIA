document.addEventListener('DOMContentLoaded', function() {
    // Existing elements
    const uploadForm = document.getElementById('uploadForm');
    const fileInput = document.getElementById('audioFile');
    const fileName = document.getElementById('fileName');
    const submitBtn = document.querySelector('.submit-btn');
    const loading = document.getElementById('loading');
    const result = document.getElementById('result');
    const error = document.getElementById('error');
    const newTranscriptionBtn = document.getElementById('newTranscription');
    const retryBtn = document.getElementById('retryBtn');

    // Audio recording elements
    const recordBtn = document.getElementById('recordBtn');
    const stopBtn = document.getElementById('stopBtn');
    const downloadBtn = document.getElementById('downloadBtn');
    const saveMemoryBtn = document.getElementById('saveMemoryBtn');
    const recordingTime = document.getElementById('recordingTime');
    const volumeBar = document.querySelector('.volume-bar');
    const sampleRateSelect = document.getElementById('sampleRate');
    const bitDepthSelect = document.getElementById('bitDepth');
    const channelsSelect = document.getElementById('channels');
    const autoTranscribeCheckbox = document.getElementById('autoTranscribe');
    
    // Memory result elements
    const memoryResult = document.getElementById('memoryResult');
    const clearMemoryBtn = document.getElementById('clearMemoryBtn');
    const newMemoryRecording = document.getElementById('newMemoryRecording');
    const transcribeMemoryBtn = document.getElementById('transcribeMemoryBtn');
    
    // Real-time transcription elements
    const realtimeTranscriptionArea = document.getElementById('realtimeTranscriptionArea');
    const realtimeTranscriptionText = document.getElementById('realtimeTranscriptionText');
    const transcriptionStatus = document.getElementById('transcriptionStatus');

    // Audio recording variables
    let mediaRecorder;
    let audioChunks = [];
    let recordedBlob = null;
    let recordingTimer;
    let startTime;
    let audioContext;
    let analyser;
    let microphone;
    let dataArray;
    let currentRecordingId = null;
    
    // Streaming variables
    let streamingSessionId = null;
    let streamingInterval = null;
    let silenceDetectionInterval = null;
    let lastAudioTime = Date.now();
    let silenceThreshold = 1000; // 1 second of silence

    // Initialize audio recording
    initializeAudioRecording();

    async function initializeAudioRecording() {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ 
                audio: {
                    echoCancellation: true,
                    noiseSuppression: true,
                    sampleRate: parseInt(sampleRateSelect.value)
                } 
            });
            
            // Setup audio context for volume visualization
            setupAudioVisualization(stream);
            
            // Setup MediaRecorder
            mediaRecorder = new MediaRecorder(stream);
            
            mediaRecorder.ondataavailable = async function(event) {
                audioChunks.push(event.data);
                
                // Send chunk to backend for streaming
                if (streamingSessionId && event.data.size > 0) {
                    try {
                        const arrayBuffer = await event.data.arrayBuffer();
                        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
                        const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
                        
                        // Convert to PCM
                        const pcmData = extractPCMFromAudioBuffer(audioBuffer);
                        
                        // Send to backend
                        const chunkResponse = await fetch('/api/stream/chunk', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                sessionId: streamingSessionId,
                                pcmData: Array.from(pcmData)
                            })
                        });
                        
                        const chunkData = await chunkResponse.json();
                        if (chunkData.success) {
                            // Update status
                            if (chunkData.isSilent) {
                                transcriptionStatus.textContent = 'Detectando silencio...';
                            } else {
                                transcriptionStatus.textContent = 'Grabando... (' + Math.floor(chunkData.bufferSize / 1024) + ' KB)';
                            }
                        }
                    } catch (error) {
                        console.error('Error sending audio chunk:', error);
                    }
                }
            };
            
            mediaRecorder.onstop = function() {
                const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
                convertToWAV(audioBlob);
                audioChunks = [];
            };
            
        } catch (error) {
            console.error('Error accessing microphone:', error);
            showError('No se pudo acceder al micrófono. Por favor, permite el acceso y recarga la página.');
        }
    }

    function setupAudioVisualization(stream) {
        audioContext = new (window.AudioContext || window.webkitAudioContext)();
        analyser = audioContext.createAnalyser();
        microphone = audioContext.createMediaStreamSource(stream);
        
        analyser.fftSize = 256;
        const bufferLength = analyser.frequencyBinCount;
        dataArray = new Uint8Array(bufferLength);
        
        microphone.connect(analyser);
        
        // Don't connect to destination to avoid feedback
        // analyser.connect(audioContext.destination);
    }

    function updateVolumeIndicator() {
        if (!analyser) return;
        
        analyser.getByteFrequencyData(dataArray);
        const average = dataArray.reduce((sum, value) => sum + value, 0) / dataArray.length;
        const percentage = (average / 255) * 100;
        volumeBar.style.width = percentage + '%';
    }

    async function convertToWAV(audioBlob) {
        try {
            const arrayBuffer = await audioBlob.arrayBuffer();
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
            
            const sampleRate = parseInt(sampleRateSelect.value);
            const bitDepth = parseInt(bitDepthSelect.value);
            const channels = parseInt(channelsSelect.value);
            
            const wavBlob = audioBufferToWav(audioBuffer, sampleRate, bitDepth, channels);
            recordedBlob = wavBlob;
            downloadBtn.disabled = false;
            saveMemoryBtn.disabled = false;
            
            // Check if auto-transcribe is enabled
            if (autoTranscribeCheckbox && autoTranscribeCheckbox.checked) {
                await autoSaveAndTranscribe();
            }
            
        } catch (error) {
            console.error('Error converting to WAV:', error);
            showError('Error al procesar el audio grabado.');
        }
    }

    function audioBufferToWav(buffer, sampleRate, bitDepth, channels) {
        const numberOfChannels = Math.min(buffer.numberOfChannels, channels);
        const length = buffer.length * numberOfChannels * (bitDepth / 8);
        
        const arrayBuffer = new ArrayBuffer(44 + length);
        const view = new DataView(arrayBuffer);
        
        // WAV file header
        writeString(view, 0, 'RIFF');
        view.setUint32(4, 36 + length, true);
        writeString(view, 8, 'WAVE');
        writeString(view, 12, 'fmt ');
        view.setUint32(16, 16, true); // fmt chunk size
        view.setUint16(20, 1, true); // PCM format
        view.setUint16(22, numberOfChannels, true);
        view.setUint32(24, sampleRate, true);
        view.setUint32(28, sampleRate * numberOfChannels * (bitDepth / 8), true); // byte rate
        view.setUint16(32, numberOfChannels * (bitDepth / 8), true); // block align
        view.setUint16(34, bitDepth, true);
        writeString(view, 36, 'data');
        view.setUint32(40, length, true);
        
        // Convert audio data
        const channelData = [];
        for (let i = 0; i < numberOfChannels; i++) {
            channelData.push(buffer.getChannelData(i));
        }
        
        let offset = 44;
        for (let i = 0; i < buffer.length; i++) {
            for (let channel = 0; channel < numberOfChannels; channel++) {
                let sample = channelData[channel][i];
                
                // Clamp sample to [-1, 1]
                sample = Math.max(-1, Math.min(1, sample));
                
                if (bitDepth === 16) {
                    sample = sample * 0x7FFF;
                    view.setInt16(offset, sample, true);
                    offset += 2;
                } else if (bitDepth === 8) {
                    sample = (sample + 1) * 0x7F;
                    view.setUint8(offset, sample);
                    offset += 1;
                } else if (bitDepth === 24) {
                    sample = sample * 0x7FFFFF;
                    const int24 = Math.round(sample);
                    view.setUint8(offset, int24 & 0xFF);
                    view.setUint8(offset + 1, (int24 >> 8) & 0xFF);
                    view.setUint8(offset + 2, (int24 >> 16) & 0xFF);
                    offset += 3;
                }
            }
        }
        
        return new Blob([arrayBuffer], { type: 'audio/wav' });
    }

    function writeString(view, offset, string) {
        for (let i = 0; i < string.length; i++) {
            view.setUint8(offset + i, string.charCodeAt(i));
        }
    }
    
    function extractPCMFromAudioBuffer(audioBuffer) {
        const sampleRate = parseInt(sampleRateSelect.value);
        const bitDepth = parseInt(bitDepthSelect.value);
        const channels = parseInt(channelsSelect.value);
        
        const numberOfChannels = Math.min(audioBuffer.numberOfChannels, channels);
        const length = audioBuffer.length * numberOfChannels * (bitDepth / 8);
        
        const pcmData = new Uint8Array(length);
        
        // Convert audio data to PCM
        const channelData = [];
        for (let i = 0; i < numberOfChannels; i++) {
            channelData.push(audioBuffer.getChannelData(i));
        }
        
        let offset = 0;
        for (let i = 0; i < audioBuffer.length; i++) {
            for (let channel = 0; channel < numberOfChannels; channel++) {
                let sample = channelData[channel][i];
                
                // Clamp sample to [-1, 1]
                sample = Math.max(-1, Math.min(1, sample));
                
                if (bitDepth === 16) {
                    sample = sample * 0x7FFF;
                    const int16 = Math.round(sample);
                    pcmData[offset++] = int16 & 0xFF;
                    pcmData[offset++] = (int16 >> 8) & 0xFF;
                } else if (bitDepth === 8) {
                    sample = (sample + 1) * 0x7F;
                    pcmData[offset++] = Math.round(sample);
                } else if (bitDepth === 24) {
                    sample = sample * 0x7FFFFF;
                    const int24 = Math.round(sample);
                    pcmData[offset++] = int24 & 0xFF;
                    pcmData[offset++] = (int24 >> 8) & 0xFF;
                    pcmData[offset++] = (int24 >> 16) & 0xFF;
                }
            }
        }
        
        return pcmData;
    }

    // Auto-save and transcribe function
    async function autoSaveAndTranscribe() {
        if (!recordedBlob) {
            showError('No hay audio grabado para transcribir.');
            return;
        }

        try {
            // Extract PCM data from WAV blob
            const arrayBuffer = await recordedBlob.arrayBuffer();
            const dataView = new DataView(arrayBuffer);
            
            // Find the data chunk and extract PCM data
            let offset = 12; // Start after RIFF header
            let pcmData = null;
            
            while (offset < arrayBuffer.byteLength - 8) {
                const chunkId = String.fromCharCode(
                    dataView.getUint8(offset),
                    dataView.getUint8(offset + 1),
                    dataView.getUint8(offset + 2),
                    dataView.getUint8(offset + 3)
                );
                const chunkSize = dataView.getUint32(offset + 4, true);
                
                if (chunkId === 'data') {
                    // Extract PCM data
                    pcmData = new Uint8Array(arrayBuffer, offset + 8, chunkSize);
                    break;
                }
                
                offset += 8 + chunkSize;
            }
            
            if (!pcmData) {
                showError('No se pudo extraer los datos PCM del audio.');
                return;
            }
            
            // Get audio metadata from selected options
            const sampleRate = parseInt(sampleRateSelect.value);
            const bitDepth = parseInt(bitDepthSelect.value);
            const channels = parseInt(channelsSelect.value);
            
            // Convert Uint8Array to regular array for JSON
            const pcmArray = Array.from(pcmData);
            
            // Prepare request
            const request = {
                pcmData: pcmArray,
                samplesPerSecond: sampleRate,
                bitsPerSample: bitDepth,
                channels: channels
            };
            
            showLoading();
            
            // Step 1: Save in memory
            const saveResponse = await fetch('/api/recording/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(request)
            });
            
            const saveData = await saveResponse.json();
            
            if (!saveData.success) {
                hideLoading();
                showError(saveData.error || 'Error al guardar en memoria');
                return;
            }
            
            currentRecordingId = saveData.id;
            
            // Check if silence was detected
            if (saveData.isSilent) {
                hideLoading();
                console.log('Silencio detectado en el audio grabado');
                showError('Se detectó silencio en el audio. Por favor, hable más alto o acérquese al micrófono.');
                return;
            }
            
            // Step 2: Transcribe immediately
            const transcribeResponse = await fetch(`/api/recording/${currentRecordingId}/transcribe`, {
                method: 'POST'
            });
            
            const transcribeData = await transcribeResponse.json();
            hideLoading();
            
            if (transcribeData.success && transcribeData.hasTranscription) {
                showResult(transcribeData);
            } else if (transcribeData.error) {
                showError(transcribeData.error);
            } else {
                showError('No se pudo transcribir el audio');
            }
            
        } catch (error) {
            hideLoading();
            console.error('Error in auto-save and transcribe:', error);
            showError('Error al transcribir automáticamente: ' + error.message);
        }
    }

    // Recording controls
    recordBtn.addEventListener('click', function() {
        if (mediaRecorder && mediaRecorder.state === 'inactive') {
            startRecording();
        }
    });

    stopBtn.addEventListener('click', function() {
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            stopRecording();
        }
    });

    downloadBtn.addEventListener('click', function() {
        if (recordedBlob) {
            const url = URL.createObjectURL(recordedBlob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `recording_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.wav`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
        }
    });
    
    saveMemoryBtn.addEventListener('click', async function() {
        if (recordedBlob) {
            try {
                // Extract PCM data from WAV blob
                const arrayBuffer = await recordedBlob.arrayBuffer();
                const dataView = new DataView(arrayBuffer);
                
                // Find the data chunk and extract PCM data
                let offset = 12; // Start after RIFF header
                let pcmData = null;
                
                while (offset < arrayBuffer.byteLength - 8) {
                    const chunkId = String.fromCharCode(
                        dataView.getUint8(offset),
                        dataView.getUint8(offset + 1),
                        dataView.getUint8(offset + 2),
                        dataView.getUint8(offset + 3)
                    );
                    const chunkSize = dataView.getUint32(offset + 4, true);
                    
                    if (chunkId === 'data') {
                        // Extract PCM data
                        pcmData = new Uint8Array(arrayBuffer, offset + 8, chunkSize);
                        break;
                    }
                    
                    offset += 8 + chunkSize;
                }
                
                if (!pcmData) {
                    showError('No se pudo extraer los datos PCM del audio.');
                    return;
                }
                
                // Get audio metadata from selected options
                const sampleRate = parseInt(sampleRateSelect.value);
                const bitDepth = parseInt(bitDepthSelect.value);
                const channels = parseInt(channelsSelect.value);
                
                // Convert Uint8Array to regular array for JSON
                const pcmArray = Array.from(pcmData);
                
                // Prepare request
                const request = {
                    pcmData: pcmArray,
                    samplesPerSecond: sampleRate,
                    bitsPerSample: bitDepth,
                    channels: channels
                };
                
                showLoading();
                
                // Make API call to save in memory
                const response = await fetch('/api/recording/start', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(request)
                });
                
                const data = await response.json();
                hideLoading();
                
                if (data.success) {
                    currentRecordingId = data.id;
                    
                    // Check if silence was detected
                    if (data.isSilent) {
                        console.log('Silencio detectado en el audio grabado');
                        showError('Se detectó silencio en el audio. Por favor, hable más alto o acérquese al micrófono.');
                    } else {
                        showMemoryResult(data);
                    }
                } else {
                    showError(data.error || 'Error al guardar en memoria');
                }
                
            } catch (error) {
                hideLoading();
                console.error('Error saving to memory:', error);
                showError('Error al guardar el audio en memoria: ' + error.message);
            }
        }
    });
    
    clearMemoryBtn.addEventListener('click', async function() {
        if (currentRecordingId) {
            try {
                showLoading();
                
                const response = await fetch(`/api/recording/${currentRecordingId}`, {
                    method: 'DELETE'
                });
                
                const data = await response.json();
                hideLoading();
                
                if (data.success) {
                    currentRecordingId = null;
                    resetForm();
                } else {
                    showError(data.error || 'Error al limpiar de memoria');
                }
                
            } catch (error) {
                hideLoading();
                console.error('Error clearing memory:', error);
                showError('Error al limpiar de memoria: ' + error.message);
            }
        }
    });
    
    newMemoryRecording.addEventListener('click', function() {
        resetForm();
    });
    
    transcribeMemoryBtn.addEventListener('click', async function() {
        if (currentRecordingId) {
            try {
                showLoading();
                
                const response = await fetch(`/api/recording/${currentRecordingId}/transcribe`, {
                    method: 'POST'
                });
                
                const data = await response.json();
                hideLoading();
                
                if (data.success && data.hasTranscription) {
                    showResult(data);
                } else if (data.error) {
                    showError(data.error);
                } else {
                    showError('No se pudo transcribir el audio');
                }
                
            } catch (error) {
                hideLoading();
                console.error('Error transcribing from memory:', error);
                showError('Error al transcribir desde memoria: ' + error.message);
            }
        }
    });

    async function startRecording() {
        audioChunks = [];
        recordedBlob = null;
        
        // Show real-time transcription area
        realtimeTranscriptionArea.classList.remove('hidden');
        realtimeTranscriptionText.value = '';
        transcriptionStatus.textContent = 'Esperando audio...';
        transcriptionStatus.className = 'transcription-status';
        
        // Start streaming session
        try {
            const sessionResponse = await fetch('/api/stream/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    samplesPerSecond: parseInt(sampleRateSelect.value),
                    bitsPerSample: parseInt(bitDepthSelect.value),
                    channels: parseInt(channelsSelect.value)
                })
            });
            
            const sessionData = await sessionResponse.json();
            if (sessionData.success) {
                streamingSessionId = sessionData.sessionId;
                lastAudioTime = Date.now();
            } else {
                showError('No se pudo iniciar la sesión de streaming');
                return;
            }
        } catch (error) {
            console.error('Error starting streaming session:', error);
            showError('Error al iniciar streaming: ' + error.message);
            return;
        }
        
        mediaRecorder.start(250); // Send chunks every 250ms
        startTime = Date.now();
        
        recordBtn.textContent = '🔴 Grabando...';
        recordBtn.classList.add('recording');
        recordBtn.disabled = true;
        stopBtn.disabled = false;
        downloadBtn.disabled = true;
        saveMemoryBtn.disabled = true;
        
        // Start timer
        recordingTimer = setInterval(updateRecordingTime, 100);
        
        // Start volume visualization
        const volumeTimer = setInterval(function() {
            if (mediaRecorder.state === 'recording') {
                updateVolumeIndicator();
            } else {
                clearInterval(volumeTimer);
                volumeBar.style.width = '0%';
            }
        }, 100);
        
        // Start silence detection
        silenceDetectionInterval = setInterval(checkForSilence, 100);
    }

    async function stopRecording() {
        if (mediaRecorder.state === 'recording') {
            mediaRecorder.stop();
        }
        
        recordBtn.textContent = '🎤 Iniciar Grabación';
        recordBtn.classList.remove('recording');
        recordBtn.disabled = false;
        stopBtn.disabled = true;
        
        clearInterval(recordingTimer);
        clearInterval(silenceDetectionInterval);
        
        // Stop streaming and get transcription
        if (streamingSessionId) {
            try {
                transcriptionStatus.textContent = 'Procesando transcripción...';
                transcriptionStatus.className = 'transcription-status processing';
                
                const stopResponse = await fetch('/api/stream/stop', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        sessionId: streamingSessionId
                    })
                });
                
                const stopData = await stopResponse.json();
                if (stopData.success && stopData.hasTranscription) {
                    realtimeTranscriptionText.value = stopData.transcribedText;
                    transcriptionStatus.textContent = 'Transcripción completada';
                    transcriptionStatus.className = 'transcription-status completed';
                } else if (stopData.error) {
                    transcriptionStatus.textContent = 'Error: ' + stopData.error;
                    transcriptionStatus.className = 'transcription-status error';
                } else {
                    transcriptionStatus.textContent = 'No se detectó voz en el audio';
                    transcriptionStatus.className = 'transcription-status';
                }
            } catch (error) {
                console.error('Error stopping streaming session:', error);
                transcriptionStatus.textContent = 'Error al procesar: ' + error.message;
                transcriptionStatus.className = 'transcription-status error';
            } finally {
                streamingSessionId = null;
            }
        }
    }
    
    async function checkForSilence() {
        if (!analyser || !streamingSessionId) return;
        
        analyser.getByteFrequencyData(dataArray);
        const average = dataArray.reduce((sum, value) => sum + value, 0) / dataArray.length;
        
        // If volume is above threshold, update last audio time
        if (average > 10) { // Threshold for "sound detected"
            lastAudioTime = Date.now();
        }
        
        // Check if silence duration exceeds threshold
        const silenceDuration = Date.now() - lastAudioTime;
        if (silenceDuration >= silenceThreshold && mediaRecorder.state === 'recording') {
            console.log('Silencio detectado por 1 segundo, deteniendo grabación...');
            transcriptionStatus.textContent = 'Silencio detectado, finalizando...';
            await stopRecording();
        }
    }

    function updateRecordingTime() {
        const elapsed = Date.now() - startTime;
        const minutes = Math.floor(elapsed / 60000);
        const seconds = Math.floor((elapsed % 60000) / 1000);
        recordingTime.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }

    // Update MediaRecorder when quality settings change
    sampleRateSelect.addEventListener('change', function() {
        if (mediaRecorder && mediaRecorder.state === 'inactive') {
            initializeAudioRecording();
        }
    });

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
        
        // Display detected language
        document.getElementById('detectedLanguage').textContent = formatLanguage(data.detectedLanguage);
        
        // Display AI response if available
        const aiResponseSection = document.getElementById('aiResponseSection');
        const aiResponseText = document.getElementById('aiResponseText');
        if (data.aiResponse && data.hasAiResponse) {
            aiResponseText.textContent = data.aiResponse;
            aiResponseSection.classList.remove('hidden');
        } else {
            aiResponseSection.classList.add('hidden');
        }
        
        // Display WAV metadata information
        document.getElementById('samplesPerSecond').textContent = formatNumber(data.samplesPerSecond);
        document.getElementById('bitsPerSample').textContent = data.bitsPerSample;
        document.getElementById('channels').textContent = data.channels === 1 ? '1 (Mono)' : data.channels === 2 ? '2 (Estéreo)' : data.channels;
        
        result.classList.remove('hidden');
    }

    function showError(message) {
        hideAllSections();
        document.getElementById('errorMessage').textContent = message;
        error.classList.remove('hidden');
    }

    function hideError() {
        error.classList.add('hidden');
        showMainSections();
    }

    function hideAllSections() {
        loading.classList.add('hidden');
        result.classList.add('hidden');
        memoryResult.classList.add('hidden');
        error.classList.add('hidden');
        document.querySelector('.recording-section').style.display = 'none';
        document.querySelector('.upload-section').style.display = 'none';
        document.querySelector('.section-divider').style.display = 'none';
    }

    function showMainSections() {
        document.querySelector('.recording-section').style.display = 'block';
        document.querySelector('.upload-section').style.display = 'block';
        document.querySelector('.section-divider').style.display = 'block';
    }

    function resetForm() {
        uploadForm.reset();
        fileName.textContent = '';
        submitBtn.disabled = true;
        
        // Reset recording state
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            stopRecording();
        }
        recordedBlob = null;
        downloadBtn.disabled = true;
        saveMemoryBtn.disabled = true;
        recordingTime.textContent = '00:00';
        volumeBar.style.width = '0%';
        currentRecordingId = null;
        
        hideAllSections();
        showMainSections();
    }
    
    function showMemoryResult(data) {
        hideAllSections();
        
        document.getElementById('memoryRecordingId').textContent = data.id;
        document.getElementById('memoryDataSize').textContent = formatNumber(data.dataSize);
        document.getElementById('memorySamplesPerSecond').textContent = formatNumber(data.samplesPerSecond);
        document.getElementById('memoryBitsPerSample').textContent = data.bitsPerSample;
        document.getElementById('memoryChannels').textContent = data.channels === 1 ? '1 (Mono)' : data.channels === 2 ? '2 (Estéreo)' : data.channels;
        
        memoryResult.classList.remove('hidden');
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
    
    function formatLanguage(languageCode) {
        if (!languageCode) {
            return 'Desconocido';
        }
        
        const languageNames = {
            'es-ES': 'Español (España)',
            'es-MX': 'Español (México)',
            'es-AR': 'Español (Argentina)',
            'en-US': 'Inglés (Estados Unidos)',
            'en-GB': 'Inglés (Reino Unido)',
            'fr-FR': 'Francés (Francia)',
            'de-DE': 'Alemán (Alemania)',
            'it-IT': 'Italiano (Italia)',
            'pt-PT': 'Portugués (Portugal)',
            'pt-BR': 'Portugués (Brasil)'
        };
        
        return languageNames[languageCode] || languageCode;
    }
});