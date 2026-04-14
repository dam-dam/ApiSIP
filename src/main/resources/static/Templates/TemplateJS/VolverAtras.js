function volverAtras(){
    const atras =document.getElementById("volver-atras");
    if(!atras){
        console.log("No se encontró el elemento con id 'volver-atras'. Asegúrate de que el HTML tenga un elemento con este id.");
        return;
    }

    atras.innerHTML=`
        <a href="javascript:void(0)" class="back-link" onclick="window.history.back()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>
            Volver al Inicio
        </a>
    `;
}