import React from 'react';

class ChatterApp extends React.Component {

    render() {
        return (
            <div className="app-container">
                {this.props.children}
            </div>
        );
    }

}

export default ChatterApp;