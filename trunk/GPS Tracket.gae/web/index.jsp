<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>GPS Tracket</title>
    </head>
    <body>
        <h1>GPS Tracket</h1>

        <ul>
            <li>/ListUsers</li>
            <li>/NewUser?ID=<u>user</u>></li>
            <li>/coordinates?ID=<u>user</u>&amp;LAT=<u>latitude</u>&amp;LON=<u>longitude</u>
                <small>
                    &amp;S=<u>speed</u>&amp;C=<u>corse</u>&amp;A=<u>altitude</u>
                    &amp;DT=<u>DateTime</u>&amp;TZ=<u>TimeZone</u>
                </small>
            </li>
            <li>/list/user?<u>user</u></li>
            <li>/list</li>
            <li>/map/user?<u>user</u></li>
            <li>/map</li>
            <li>/kml/user?<u>user</u></li>
            <li>/kml</li>
            <li>/m/u?<u>user</u></li>
            <li>/m</li>
            <li>/kml-html/user?<u>user</u></li>
            <li>/kml-html</li>
        </ul>

    </body>
</html>
