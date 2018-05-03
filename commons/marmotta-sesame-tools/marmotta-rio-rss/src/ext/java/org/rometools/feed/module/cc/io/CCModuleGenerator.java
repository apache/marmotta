/*
 * CCModuleGenerator.java
 *
 * Created on November 20, 2005, 5:23 PM
 *
 * This library is provided under dual licenses. 
 * You may choose the terms of the Lesser General Public License or the Apache
 * License at your discretion.
 *
 *  Copyright (C) 2005  Robert Cooper, Temple of the Screaming Penguin
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.   
 */

package org.rometools.feed.module.cc.io;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.rometools.feed.module.cc.CreativeCommons;
import org.rometools.feed.module.cc.CreativeCommonsImpl;
import org.rometools.feed.module.cc.types.License;

import java.util.HashSet;
import java.util.Set;

/**
 * @version $Revision: 1.1 $
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 */
public class CCModuleGenerator implements ModuleGenerator{
    
    private static final Namespace RSS1 = Namespace.getNamespace( "cc", CreativeCommonsImpl.RSS1_URI );
    private static final Namespace RSS2 = Namespace.getNamespace( "creativeCommons", CreativeCommonsImpl.RSS2_URI);
    private static final Namespace RSS = Namespace.getNamespace("http://purl.org/rss/1.0/");
    private static final Namespace RDF = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static final HashSet NAMESPACES = new HashSet();
    static{
	NAMESPACES.add( RSS1 );
	NAMESPACES.add( RSS2 );
	NAMESPACES.add( RDF );
    }
    
    /**
     * Creates a new instance of CCModuleGenerator
     */
    public CCModuleGenerator() {
	super();
    }

    public void generate(Module module, Element element) {
	Element root = element;
	while( root.getParentElement() != null ){
	    root = root.getParentElement();
	}
	if( root.getNamespace().equals( RDF )|| root.getNamespace().equals( RSS )){
	    generateRSS1( (CreativeCommons) module, element);
	} else {
	    generateRSS2( (CreativeCommons) module, element);
	}
    }

    public Set getNamespaces() {
	return NAMESPACES;
    }

    public String getNamespaceUri() {
	return CreativeCommons.URI;
    }
    
    private void generateRSS1( CreativeCommons module, Element element ){
	//throw new RuntimeException( "Generating RSS1 Feeds not currently Supported.");
	
	System.out.println(element.getName());
	if( element.getName().equals("channel")){
	    // Do all licenses list.
	    License[] all = module.getAllLicenses();
        for (License anAll : all) {
            Element license = new Element("License", RSS1);
            license.setAttribute("about", anAll.getValue(), RDF);
            License.Behaviour[] permits = anAll.getPermits();
            for (int j = 0; permits != null && j < permits.length; j++) {
                Element permit = new Element("permits", RSS1);
                permit.setAttribute("resource", permits[j].toString(), RDF);
                license.addContent(permit);
            }
            License.Behaviour[] requires = anAll.getPermits();
            for (int j = 0; requires != null && j < requires.length; j++) {
                Element permit = new Element("requires", RSS1);
                permit.setAttribute("resource", permits[j].toString(), RDF);
                license.addContent(permit);
            }
            System.out.println("Is Root?" + element.getParentElement());
            element.getParentElement().addContent(license);
        }
	}
	 
	//Do local licenses
	License[] licenses = module.getLicenses();
        for (License license1 : licenses) {
            Element license = new Element("license", RSS1);
            license.setAttribute("resource", license1.getValue(), RDF);
            element.addContent(license);
        }
	
    }
    
    private void generateRSS2( CreativeCommons module, Element element ){
	License[] licenses = module.getLicenses();
        for( int i=0; licenses != null &&  i < licenses.length; i++ ){
	    Element license = new Element( "license", RSS2 );
	    license.setText( licenses[i].getValue() );
	    element.addContent( license );
	}
    }
}
