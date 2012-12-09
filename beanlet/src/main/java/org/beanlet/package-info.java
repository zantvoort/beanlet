/*
 * ============================================================================
 * GNU Lesser General Public License
 * ============================================================================
 *
 * Beanlet - JSE Application Container.
 * Copyright (C) 2006  Leon van Zantvoort
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 * 
 * Leon van Zantvoort
 * 243 Acalanes Drive #11
 * Sunnyvale, CA 94086
 * USA
 *
 * zantvoort@users.sourceforge.net
 * http://beanlet.org
 */

/**
 * Contains all of the core beanlet interfaces and bootstrap classes.
 *
 * <p><h3>Beanlet Scopes</h3>
 * The Beanlet core packages come with three object scopes:
 * <ul>
 * <li>Vanilla beanlets are the most straight forward type
 * of beanlets within this specification. Unlike the scopes described below, 
 * vanilla beanlets do not have a stub that controls access to the beanlet 
 * instance. Clients have access to the actual object. In short, 
 * they can invoke methods directly on the beanlet instance.<br>
 * A new beanlet instance is created for each beanlet that is explicitely 
 * requested throuh the APIs, or implicitly through dependency injection.<br> 
 * Vanilla scoped beanlets can also be configured to be {@link org.beanlet.Singleton}. In
 * this case, the container guarantees that only a single instance of this 
 * beanlet exists at the same time. It is always this particular instance that
 * is returned when this beanlet is requested.
 * <li>{@link org.beanlet.Stateful} beanlets do not provide direct access to the
 * underlying objects. Instead, clients obtain a stub, which delegates 
 * invocations to the underlying instance. In case of stateful beanlets, 
 * invocations are always delegated to the same instance.<br>
 * A new beanlet instance is created for each beanlet that is explicitely 
 * requested throuh the APIs, or implicitly through dependency injection.<br>
 * Stateful beanlets are non {@code reentrant} by default, which means that only 
 * one thread can invoke a method of the beanlet instance at the same time. 
 * This feature is provided by the stub, which controls all access to the 
 * underlying instance. Stateful beanlets can also be configured to be reentrant.
 * <li>{@link org.beanlet.Stateless} beanlets are the counterpart of
 * stateful beanlets. Stateless beanlet instances are pooled. Methods called on
 * stateless beanlet stubs are delegated to one of a pool of instances. 
 * If multiple invocations are made on the same stub reference, different
 * beanlet instances can be used to perform the call.<br>
 * Beanlet instances are added and removed from the pool at container's 
 * discretion. Requesting a beanlet stub through the APIs, or dependency 
 * injection does not necessarily result in the creation of a new beanlet 
 * instance. In general, the number of pooled beanlet instances depends on the 
 * number of concurrent requests made to these stubs.</br>
 * Stateless beanlets can be configured to be {@code reentrant} and 
 * {@code singleton}.
 * </ul>
 * </p>
 * 
 * <p><h3>Static versus Non-Static</h3>
 * Beanlets are created in either a static or a non-static context. Static, in
 * this context, means that beanlets are created independently from the 
 * current state of the container or application. Static beanlets are created
 * at the container's discretion and therefore do not allow applications to 
 * pass any state to this component while it is being created. Non-static 
 * beanlets are only created upon application request. Applications can pass
 * objects - or state if you will - by using wiring {@code BY_INFO} dependency 
 * injection. Use the {@link org.beanlet.BeanletMetaData} interface to find out
 * if a beanlet is static programmatically.</p>
 * 
 * <p>The static property of a beanlet is derived from the selected beanlet 
 * scope, i.e., singleton vanilla beanlets are static, where non-singleton 
 * vanilla beanlets are non-static. The following table shows the static 
 * property for all core beanlet scopes.</p>
 * <br>
 * <p><table width="400" border="1">
 * <tr><td></td><th>singleton</th><th>reentrant</th><th><i>static</i></th></tr>
 * <tr><th>Vanilla</th><td>true</td><td>N/A</td><td>true</td></tr>
 * <tr><th>Vanilla</th><td>false</td><td>N/A</td><td>false</td></tr>
 * <tr><th>Stateful</th><td>true/false</td><td>true/false</td><td>false</td></tr>
 * <tr><th>Stateless</th><td>true/false</td><td>true/false</td><td>true</td></tr>
 * </table></p>
 * <br>
 * <p>The static property has also effect on the lifecycle management of beanlets
 * as is explained by the next paragraph.</p>
 *
 * <p><h3>Beanlet Lifecycle Management</h3>
 * The container is responsible for managaging the lifecycle of beanlets. Two
 * lifecycle levels can be identified. That is, at instance-level and at 
 * business-level.
 * 
 * <h4>Instance-Level</h4>
 * <p>Instance-level lifecycle methods are invoked during the process of creating 
 * or destroying individual beanlet instances. These lifecycle methods are 
 * marked with the {@link org.beanlet.PostConstruct} and 
 * {@link org.beanlet.PreDestroy} annotations, respectively. These methods are
 * invoked in an unspecified context. This means that transactions, for 
 * instance, are not supported for these methods.<br>
 * The {@code PostConstruct} method is invoked right after dependency 
 * injection. This method has to be completed before any business methods can 
 * be invoked on the instance. The {@code PreDestroy} method is the last method
 * to be invoked on the beanlet instance. Whether the {@code PreDestroy} method
 * is actually invoked depends on the {@link org.beanlet.RetentionPolicy} 
 * specified by the beanlet.
 * 
 * <h4>Business-Level</h4>
 * Business-level lifecycle methods are in genereral no different from any other 
 * business methods, except that they are invoked by the container directly.
 * These lifecycle methods are marked with the {@link org.beanlet.Start} and
 * {@link org.beanlet.Stop} annotations, respectively.<br>
 * For non-static beanlets, business-level lifeycle methods are processed at
 * beanlet instance level. This is quite similar to the instance-level lifecycle
 * methods described above, except that the business-level lifecycle methods do
 * run in a specified context (and thus support transactions). As a result,
 * the {@code Start} and {@code Stop} methods of a non-static beanlet can be 
 * invoked multiple times, namely once per constructed instance.
 * <br>
 * For static beanlets, business-level lifecycle methods are processed at 
 * beanlet level. If a beanlet specifies a {@code Start} method, the container
 * requests a beanlet instance during deployment and invokes this {@code Start} 
 * method. The {@code Stop} method is invoked if the beanlet is being 
 * undeployed. In other words, the {@code Start} and {@code Stop} for static 
 * beanlets are invoked only once.</p>
 * 
 * <!--p><h3>Beanlet Method Execution and Scheduling</h3>
 * The inner workings of execution and scheduling is described at 
 * {@link org.beanlet.Execute} and {@link org.beanlet.Schedule}.
 * </p-->
 * 
 * <!--p><h3>Beanlet Interceptors</h3>
 * </p-->
 *
 * <!--p><h3>Beanlet Events and MetaData</h3>
 * </p-->
 *
 * <p><h3>Beanlet Definitions</h3>
 * Throughout this API, the following definitions are used:
 * <ul>
 * <li><b>Beanlet</b> is, in general, the name used to identify the objects 
 * clients interact with. In other words, the name 'beanlet' is used for the 
 * objects that are injected, and objects that are returned by the APIs (
 * {@link org.beanlet.BeanletApplicationContext} and
 * {@link org.beanlet.BeanletReference}). The 'beanlet' might 
 * actually be a 'beanlet instance' in case of vanilla beanlets, or a 
 * 'beanlet stub' for stateful or stateless beanlets.
 * <li><b>Beanlet instance</b> is the underlying object of a beanlet. Beanlet
 * instances provide the actual business logic of the beanlet. The lifecycle of
 * beanlet instances is fully controlled by the container.
 * <li><b>Beanlet stub</b> acts as a proxy and controls access to the underlying 
 * beanlet instances.
 * <li><b>Beanlet definition</b> stands for the component declared at the
 * beanlet xml-files. More specificly, a beanlet is defined by the 
 * {@code <beanlet>} xml-tag. A 'beanlet definition' embodies all properties 
 * that constitute a beanlet, like its name, type, classloader and meta data.
 * The 'beanlet defintion' can be accessed programatically through the
 * {@link org.beanlet.BeanletMetaData} interface.
 * <li><b>Beanlet type</b> is the type as expressed by the {@code <beanlet>} 
 * xml-tag. The beanlet type is not necessarily the same as the type of the 
 * beanlet instance. The beanlet instance may be a subclass of the expressed 
 * beanlet type. More specifically, the type of the beanlet instance can vary 
 * for each individual instance. Therefore all properties of the beanlet are 
 * derived from the beanlet type, which is static. The beanlet type can be 
 * obtained via {@link org.beanlet.BeanletMetaData}.<br>
 * </ul>
 */
package org.beanlet;

