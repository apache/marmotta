/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.jndi;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;

import java.util.*;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class MarmottaContext implements Context {

    private Hashtable<Object, Object>      environment;

    private HashMap<Name,Object> bindings;

    private HashMap<String,ObjectFactory> factories;

    private static Properties parseProperties = new Properties();
    static {
        parseProperties.put("jndi.syntax.direction","left_to_right");
        parseProperties.put("jndi.syntax.separator","/");
    }

    public MarmottaContext(Hashtable<Object, Object> environment) {
        this.environment = environment;

        this.bindings  = new HashMap<Name, Object>();
        this.factories = new HashMap<String, ObjectFactory>();
    }

    private ObjectFactory getObjectFactory(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(factories.containsKey(className))
            return factories.get(className);
        else {
            ObjectFactory factory = (ObjectFactory) Class.forName(className).newInstance();
            factories.put(className,factory);
            return factory;
        }
    }

    /**
     * Retrieves the named object.
     * If <tt>name</tt> is empty, returns a new instance of this context
     * (which represents the same naming context as this context, but its
     * environment may be modified independently and it may be accessed
     * concurrently).
     *
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #lookup(String)
     * @see #lookupLink(javax.naming.Name)
     */
    @Override
    public Object lookup(Name name) throws NamingException {
        if(name.size() == 0) {
            // clone current context
            MarmottaContext clone = new MarmottaContext(new Hashtable<Object, Object>(this.environment));
            clone.bindings = new HashMap<Name, Object>(this.bindings);
            return clone;
        } else if(name.size() > 1) {
            // look in subcontexts
            if(bindings.containsKey(name.getPrefix(1))) {
                Object subcontext = bindings.get(name.getPrefix(1));
                if(subcontext instanceof Context) return ((Context) subcontext).lookup(name.getSuffix(1));
                else
                    throw new NotContextException("the name "+name.getPrefix(1)+" does not identify a context");
            } else
                throw new NameNotFoundException("the name "+name.getPrefix(1)+" is not bound");

        } else if(bindings.containsKey(name)) {
            Object value = bindings.get(name);
            try {
                if(value instanceof Reference) {
                    ObjectFactory factory = getObjectFactory(((Reference) value).getFactoryClassName());
                    return factory.getObjectInstance(null,name,this,environment);
                } else if(value instanceof Referenceable) {
                    ObjectFactory factory = getObjectFactory(((Referenceable) value).getReference().getFactoryClassName());
                    return factory.getObjectInstance(null,name,this,environment);
                } else
                    return value;
            } catch(Exception ex) {
                throw new NamingException("could not create object: "+ex.getMessage());
            }

        } else
            throw new NameNotFoundException("name "+name+" could not be found");
    }

    /**
     * Retrieves the named object.
     * See {@link #lookup(javax.naming.Name)} for details.
     *
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public Object lookup(String name) throws NamingException {
        return lookup(new CompoundName(name,parseProperties));
    }

    /**
     * Binds a name to an object.
     * All intermediate contexts and the target context (that named by all
     * but terminal atomic component of the name) must already exist.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws javax.naming.NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if object did not supply all mandatory attributes
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #bind(String, Object)
     * @see #rebind(javax.naming.Name, Object)
     * @see javax.naming.directory.DirContext#bind(javax.naming.Name, Object,
     *      javax.naming.directory.Attributes)
     */
    @Override
    public void bind(Name name, Object obj) throws NamingException {
        if(name.size() == 0)
            throw new InvalidNameException("the name passed to bind() is not valid");
        else if(name.size() > 1) {
            // we try getting the subcontext with the given name if it exists or create a new one if it does not
            // exist, and then pass over to the subcontext's bind() operation
            // look in subcontexts
            if(bindings.containsKey(name.getPrefix(1))) {
                Object subcontext = bindings.get(name.getPrefix(1));
                if(subcontext instanceof Context) {
                    ((Context) subcontext).bind(name.getSuffix(1),obj);
                } else
                    throw new NotContextException("the name "+name.getPrefix(1)+" does not identify a context");
            } else {
                Context subcontext = createSubcontext(name.getPrefix(1));
                subcontext.bind(name.getSuffix(1),obj);
            }
        } else if(bindings.containsKey(name))
            throw new NameAlreadyBoundException("name "+name+" is already bound in this context");
        else {
            bindings.put(name,obj);
        }
    }

    /**
     * Binds a name to an object.
     * See {@link #bind(javax.naming.Name, Object)} for details.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws javax.naming.NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if object did not supply all mandatory attributes
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public void bind(String name, Object obj) throws NamingException {
        bind(new CompoundName(name,parseProperties),obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * All intermediate contexts and the target context (that named by all
     * but terminal atomic component of the name) must already exist.
     * <p/>
     * <p> If the object is a <tt>DirContext</tt>, any existing attributes
     * associated with the name are replaced with those of the object.
     * Otherwise, any existing attributes associated with the name remain
     * unchanged.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws javax.naming.directory.InvalidAttributesException if object did not supply all mandatory attributes
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #rebind(String, Object)
     * @see #bind(javax.naming.Name, Object)
     * @see javax.naming.directory.DirContext#rebind(javax.naming.Name, Object,
     *      javax.naming.directory.Attributes)
     * @see javax.naming.directory.DirContext
     */
    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        if(name.size() == 0)
            throw new InvalidNameException("the name passed to bind() is not valid");
        else if(name.size() > 1) {
            // we try getting the subcontext with the given name if it exists or create a new one if it does not
            // exist, and then pass over to the subcontext's bind() operation
            // look in subcontexts
            if(bindings.containsKey(name.getPrefix(1))) {
                Object subcontext = bindings.get(name.getPrefix(1));
                if(subcontext instanceof Context) {
                    ((Context) subcontext).bind(name.getSuffix(1),obj);
                } else
                    throw new NotContextException("the name "+name.getPrefix(1)+" does not identify a context");
            } else {
                Context subcontext = createSubcontext(name.getPrefix(1));
                subcontext.bind(name.getSuffix(1),obj);
            }
        } else {
            bindings.put(name,obj);
        }
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * See {@link #rebind(javax.naming.Name, Object)} for details.
     *
     * @param name the name to bind; may not be empty
     * @param obj  the object to bind; possibly null
     * @throws javax.naming.directory.InvalidAttributesException if object did not supply all mandatory attributes
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public void rebind(String name, Object obj) throws NamingException {
        rebind(new CompoundName(name,parseProperties),obj);
    }

    /**
     * Unbinds the named object.
     * Removes the terminal atomic name in <code>name</code>
     * from the target context--that named by all but the terminal
     * atomic part of <code>name</code>.
     * <p/>
     * <p> This method is idempotent.
     * It succeeds even if the terminal atomic name
     * is not bound in the target context, but throws
     * <tt>NameNotFoundException</tt>
     * if any of the intermediate contexts do not exist.
     * <p/>
     * <p> Any attributes associated with the name are removed.
     * Intermediate contexts are not changed.
     *
     * @param name the name to unbind; may not be empty
     * @throws javax.naming.NameNotFoundException if an intermediate context does not exist
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #unbind(String)
     */
    @Override
    public void unbind(Name name) throws NamingException {
        if(name.size() == 0)
            throw new InvalidNameException("an empty name cannot be unbound");
        else if(name.size() > 1) {
            // look in subcontexts
            if(bindings.containsKey(name.getPrefix(1))) {
                Object subcontext = bindings.get(name.getPrefix(1));
                if(subcontext instanceof Context) {
                    ((Context) subcontext).unbind(name.getSuffix(1));
                } else
                    throw new NotContextException("the name "+name.getPrefix(1)+" does not identify a context");
            } else
                throw new NameNotFoundException("the name "+name.getPrefix(1)+" is not bound");
        } else {
            bindings.remove(name);
        }
    }

    /**
     * Unbinds the named object.
     * See {@link #unbind(javax.naming.Name)} for details.
     *
     * @param name the name to unbind; may not be empty
     * @throws javax.naming.NameNotFoundException if an intermediate context does not exist
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public void unbind(String name) throws NamingException {
        unbind(new CompoundName(name,parseProperties));
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds
     * the old name.  Both names are relative to this context.
     * Any attributes associated with the old name become associated
     * with the new name.
     * Intermediate contexts of the old name are not changed.
     *
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws javax.naming.NameAlreadyBoundException if <tt>newName</tt> is already bound
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #rename(String, String)
     * @see #bind(javax.naming.Name, Object)
     * @see #rebind(javax.naming.Name, Object)
     */
    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        /* Confirm that this works.  We might have to catch the exception */
        Object old = lookup(oldName);
        if(newName.isEmpty()) throw new InvalidNameException("Cannot bind to empty name");

        if(old == null) throw new NamingException("Name '" + oldName + "' not found.");

        /* If the new name is bound throw a NameAlreadyBoundException */
        if(lookup(newName) != null) throw new NameAlreadyBoundException("Name '" + newName + "' already bound");

        unbind(oldName);
        unbind(newName);
        bind(newName, old);
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds
     * the old name.
     * See {@link #rename(javax.naming.Name, javax.naming.Name)} for details.
     *
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws javax.naming.NameAlreadyBoundException if <tt>newName</tt> is already bound
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public void rename(String oldName, String newName) throws NamingException {
        rename(new CompoundName(oldName,parseProperties),new CompoundName(newName,parseProperties));
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * class names of objects bound to them.
     * The contents of any subcontexts are not included.
     * <p/>
     * <p> If a binding is added to or removed from this context,
     * its effect on an enumeration previously returned is undefined.
     *
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the
     * bindings in this context.  Each element of the
     * enumeration is of type <tt>NameClassPair</tt>.
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #list(String)
     * @see #listBindings(javax.naming.Name)
     * @see javax.naming.NameClassPair
     */
    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        if(name == null || name.size() == 0)
            return new NameClassEnumerationImpl(this.bindings);
        else {
            Object subcontext = bindings.get(name.getPrefix(1));
            if(subcontext == null)
                throw new NameNotFoundException("subcontext with name "+name.getPrefix(1)+" does not exist");
            else if(subcontext instanceof Context) return ((Context) subcontext).list(name.getSuffix(1));
            else
                throw new NotContextException("object with name "+name.getPrefix(1)+" is not a context");
        }
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * class names of objects bound to them.
     * See {@link #list(javax.naming.Name)} for details.
     *
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the
     * bindings in this context.  Each element of the
     * enumeration is of type <tt>NameClassPair</tt>.
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return list(new CompoundName(name,parseProperties));
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them.
     * The contents of any subcontexts are not included.
     * <p/>
     * <p> If a binding is added to or removed from this context,
     * its effect on an enumeration previously returned is undefined.
     *
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context.
     * Each element of the enumeration is of type
     * <tt>Binding</tt>.
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #listBindings(String)
     * @see #list(javax.naming.Name)
     * @see javax.naming.Binding
     */
    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        if(name == null || name.size() == 0)
            return new BindingEnumerationImpl(this.bindings);
        else {
            Object subcontext = bindings.get(name.getPrefix(1));
            if(subcontext == null)
                throw new NameNotFoundException("subcontext with name "+name.getPrefix(1)+" does not exist");
            else if(subcontext instanceof Context) return ((Context) subcontext).listBindings(name.getSuffix(1));
            else
                throw new NotContextException("object with name "+name.getPrefix(1)+" is not a context");
        }
    }

    /**
     * Enumerates the names bound in the named context, along with the
     * objects bound to them.
     * See {@link #listBindings(javax.naming.Name)} for details.
     *
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context.
     * Each element of the enumeration is of type
     * <tt>Binding</tt>.
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return listBindings(new CompoundName(name, parseProperties));
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * Any attributes associated with the name are also removed.
     * Intermediate contexts are not destroyed.
     * <p/>
     * <p> This method is idempotent.
     * It succeeds even if the terminal atomic name
     * is not bound in the target context, but throws
     * <tt>NameNotFoundException</tt>
     * if any of the intermediate contexts do not exist.
     * <p/>
     * <p> In a federated naming system, a context from one naming system
     * may be bound to a name in another.  One can subsequently
     * look up and perform operations on the foreign context using a
     * composite name.  However, an attempt destroy the context using
     * this composite name will fail with
     * <tt>NotContextException</tt>, because the foreign context is not
     * a "subcontext" of the context in which it is bound.
     * Instead, use <tt>unbind()</tt> to remove the
     * binding of the foreign context.  Destroying the foreign context
     * requires that the <tt>destroySubcontext()</tt> be performed
     * on a context from the foreign context's "native" naming system.
     *
     * @param name the name of the context to be destroyed; may not be empty
     * @throws javax.naming.NameNotFoundException if an intermediate context does not exist
     * @throws javax.naming.NotContextException if the name is bound but does not name a
     * context, or does not name a context of the appropriate type
     * @throws javax.naming.ContextNotEmptyException if the named context is not empty
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #destroySubcontext(String)
     */
    @Override
    public void destroySubcontext(Name name) throws NamingException {
        unbind(name);
    }

    /**
     * Destroys the named context and removes it from the namespace.
     * See {@link #destroySubcontext(javax.naming.Name)} for details.
     *
     * @param name the name of the context to be destroyed; may not be empty
     * @throws javax.naming.NameNotFoundException if an intermediate context does not exist
     * @throws javax.naming.NotContextException if the name is bound but does not name a
     * context, or does not name a context of the appropriate type
     * @throws javax.naming.ContextNotEmptyException if the named context is not empty
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public void destroySubcontext(String name) throws NamingException {
        unbind(name);
    }

    /**
     * Creates and binds a new context.
     * Creates a new context with the given name and binds it in
     * the target context (that named by all but terminal atomic
     * component of the name).  All intermediate contexts and the
     * target context must already exist.
     *
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws javax.naming.NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if creation of the subcontext requires specification of
     * mandatory attributes
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #createSubcontext(String)
     * @see javax.naming.directory.DirContext#createSubcontext
     */
    @Override
    public Context createSubcontext(Name name) throws NamingException {
        MarmottaContext subcontext = new MarmottaContext(new Hashtable<Object, Object>(this.environment));
        bind(name,subcontext);
        return subcontext;
    }

    /**
     * Creates and binds a new context.
     * See {@link #createSubcontext(javax.naming.Name)} for details.
     *
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws javax.naming.NameAlreadyBoundException if name is already bound
     * @throws javax.naming.directory.InvalidAttributesException if creation of the subcontext requires specification of
     * mandatory attributes
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public Context createSubcontext(String name) throws NamingException {
        return createSubcontext(new CompoundName(name, parseProperties));
    }

    /**
     * Retrieves the named object, following links except
     * for the terminal atomic component of the name.
     * If the object bound to <tt>name</tt> is not a link,
     * returns the object itself.
     *
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>, not following the
     * terminal link (if any).
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #lookupLink(String)
     */
    @Override
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name);
    }

    /**
     * Retrieves the named object, following links except
     * for the terminal atomic component of the name.
     * See {@link #lookupLink(javax.naming.Name)} for details.
     *
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>, not following the
     * terminal link (if any)
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    /**
     * Retrieves the parser associated with the named context.
     * In a federation of namespaces, different naming systems will
     * parse names differently.  This method allows an application
     * to get a parser for parsing names into their atomic components
     * using the naming convention of a particular naming system.
     * Within any single naming system, <tt>NameParser</tt> objects
     * returned by this method must be equal (using the <tt>equals()</tt>
     * test).
     *
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     * components
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #getNameParser(String)
     * @see javax.naming.CompoundName
     */
    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return new LMFNameParser();
    }

    /**
     * Retrieves the parser associated with the named context.
     * See {@link #getNameParser(javax.naming.Name)} for details.
     *
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     * components
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return new LMFNameParser();
    }

    /**
     * Composes the name of this context with a name relative to
     * this context.
     * Given a name (<code>name</code>) relative to this context, and
     * the name (<code>prefix</code>) of this context relative to one
     * of its ancestors, this method returns the composition of the
     * two names using the syntax appropriate for the naming
     * system(s) involved.  That is, if <code>name</code> names an
     * object relative to this context, the result is the name of the
     * same object, but relative to the ancestor context.  None of the
     * names may be null.
     * <p/>
     * For example, if this context is named "wiz.com" relative
     * to the initial context, then
     * <pre>
     * 	composeName("east", "wiz.com")	</pre>
     * might return <code>"east.wiz.com"</code>.
     * If instead this context is named "org/research", then
     * <pre>
     * 	composeName("user/jane", "org/research")	</pre>
     * might return <code>"org/research/user/jane"</code> while
     * <pre>
     * 	composeName("user/jane", "research")	</pre>
     * returns <code>"research/user/jane"</code>.
     *
     * @param name   a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #composeName(String, String)
     */
    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        Name retName = (Name)prefix.clone();
        retName.addAll(name);
        return retName;
    }

    /**
     * Composes the name of this context with a name relative to
     * this context.
     * See {@link #composeName(javax.naming.Name, javax.naming.Name)} for details.
     *
     * @param name   a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of <code>prefix</code> and <code>name</code>
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return composeName(new CompoundName(name,parseProperties), new CompoundName(prefix,parseProperties)).toString();
    }

    /**
     * Adds a new environment property to the environment of this
     * context.  If the property already exists, its value is overwritten.
     * See class description for more details on environment properties.
     *
     * @param propName the name of the environment property to add; may not be null
     * @param propVal  the value of the property to add; may not be null
     * @return the previous value of the property, or null if the property was
     * not in the environment before
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #getEnvironment()
     * @see #removeFromEnvironment(String)
     */
    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return environment.put(propName,propVal);
    }

    /**
     * Removes an environment property from the environment of this
     * context.  See class description for more details on environment
     * properties.
     *
     * @param propName the name of the environment property to remove; may not be null
     * @return the previous value of the property, or null if the property was
     * not in the environment
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #getEnvironment()
     * @see #addToEnvironment(String, Object)
     */
    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return environment.remove(propName);
    }

    /**
     * Retrieves the environment in effect for this context.
     * See class description for more details on environment properties.
     * <p/>
     * <p> The caller should not make any changes to the object returned:
     * their effect on the context is undefined.
     * The environment of this context may be changed using
     * <tt>addToEnvironment()</tt> and <tt>removeFromEnvironment()</tt>.
     *
     * @return the environment of this context; never null
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @see #addToEnvironment(String, Object)
     * @see #removeFromEnvironment(String)
     */
    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return new Hashtable<Object, Object>(environment);
    }

    /**
     * Closes this context.
     * This method releases this context's resources immediately, instead of
     * waiting for them to be released automatically by the garbage collector.
     * <p/>
     * <p> This method is idempotent:  invoking it on a context that has
     * already been closed has no effect.  Invoking any other method
     * on a closed context is not allowed, and results in undefined behaviour.
     *
     * @throws javax.naming.NamingException if a naming exception is encountered
     */
    @Override
    public void close() throws NamingException {
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * <p/>
     * <p> Many naming services have a notion of a "full name" for objects
     * in their respective namespaces.  For example, an LDAP entry has
     * a distinguished name, and a DNS record has a fully qualified name.
     * This method allows the client application to retrieve this name.
     * The string returned by this method is not a JNDI composite name
     * and should not be passed directly to context methods.
     * In naming systems for which the notion of full name does not
     * make sense, <tt>OperationNotSupportedException</tt> is thrown.
     *
     * @return this context's name in its own namespace; never null
     * @throws javax.naming.OperationNotSupportedException if the naming system does
     * not have the notion of a full name
     * @throws javax.naming.NamingException if a naming exception is encountered
     * @since 1.3
     */
    @Override
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException("no fullname support");
    }


    private static class NameClassEnumerationImpl implements NamingEnumeration<NameClassPair> {

        Iterator<Map.Entry<Name,Object>> iterator;

        private NameClassEnumerationImpl(Map<Name, Object> names) {
            iterator = names.entrySet().iterator();
        }

        @Override
        public NameClassPair next() throws NamingException {
            Map.Entry<Name,Object> element = iterator.next();

            return new NameClassPair(element.getKey().toString(),element.getValue().getClass().getName());
        }

        @Override
        public boolean hasMore() throws NamingException {
            return iterator.hasNext();
        }

        @Override
        public void close() throws NamingException {
        }

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public NameClassPair nextElement() {
            try {
                return next();
            } catch (NamingException e) {
                throw new NoSuchElementException("no such element");
            }
        }
    }

    private static class BindingEnumerationImpl implements NamingEnumeration<Binding> {
        Iterator<Map.Entry<Name,Object>> iterator;

        private BindingEnumerationImpl(Map<Name, Object> names) {
            iterator = names.entrySet().iterator();
        }

        @Override
        public Binding next() throws NamingException {
            Map.Entry<Name,Object> element = iterator.next();

            return new Binding(element.getKey().toString(),element.getValue());
        }

        @Override
        public boolean hasMore() throws NamingException {
            return iterator.hasNext();
        }

        @Override
        public void close() throws NamingException {
        }

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public Binding nextElement() {
            try {
                return next();
            } catch (NamingException e) {
                throw new NoSuchElementException("no such element");
            }
        }
    }

    private static class LMFNameParser implements NameParser {
        @Override
        public Name parse(String name) throws NamingException {
            return new CompoundName(name,parseProperties);
        }
    }
}
