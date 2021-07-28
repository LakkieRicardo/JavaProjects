import ReactDOM from 'react-dom';

import ChatterApp from './ChatterApp';
import ServerConnection from './ServerConnection';
import ServerListing from './ServerListing';

import './styles/style.css';

/*
    Normally rendering directly into the body element is discouraged because other scripts may modify the body and cause issues.
    However, Chatter 2 Client(web) does not have any scripts that modifies the body element.
*/

ReactDOM.render(
    <ChatterApp>
        <ServerConnection />
        <ServerListing />
    </ChatterApp>,
    document.querySelector("body")
);