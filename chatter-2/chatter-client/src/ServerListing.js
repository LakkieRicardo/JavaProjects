import React, { useEffect, useRef } from 'react';


// dummy data
const servers = [
    {
        id: 0,
        name: "TestServer",
        address: "127.0.0.1:5000"
    },
    {
        id: 204,
        name: "Second Server",
        address: "anotheraddress.io:1234"
    },
    {
        id: 42,
        name: "Third Server",
        address: "thirdaddress.pw:5678"
    },
    {
        id: 146,
        name: "Fourth Server",
        address: "whatafourthaddress.blackfriday:42069"
    }
];

const ServerListing = () => { 
    
    const listingContainerElem = useRef(null);

    function toggleVisibility () {
        if (!listingContainerElem || !listingContainerElem.current) return;
        const visible = listingContainerElem.current.classList.contains("server-listing-container--hidden");
        if (visible) {
            listingContainerElem.current.classList.remove("server-listing-container--hidden");
            listingContainerElem.current.querySelector("button.listing-toggle").innerHTML = "&#9658;";
        } else {
            listingContainerElem.current.classList.add("server-listing-container--hidden");
            listingContainerElem.current.querySelector("button.listing-toggle").innerHTML = "&#9664;";
        }
    }

    useEffect(() => {
        window.addEventListener("keydown", (ev) => {
            if (ev.key === "Tab") {
                ev.preventDefault();
                ev.stopPropagation();
                toggleVisibility();
            }
        });
    });

    return(
        <div className="app-card server-listing-container" ref={listingContainerElem}>
            <h1>
                Servers
            </h1>
            <div className="server-listing">
                {servers.map((server) => (
                    <div key={server.id}>
                        <span className="server-listing-item">{server.name} <span>({server.address})</span></span>
                        <button className="server-listing-item">Connect</button>
                    </div>
                ))}
            </div>
            <button className="listing-toggle" onClick={toggleVisibility}>
                &#9658;
            </button>
        </div>
    );
};

export default ServerListing;