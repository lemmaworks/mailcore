package org.apache.james.mailrepository;

import org.apache.avalon.cornerstone.services.store.StreamRepository;
import org.apache.james.core.MimeMessageUtil;
import org.apache.mailet.Mail;

import javax.mail.MessagingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

/**
 * This class provides an inputStream for a Mail object.
 * If the Mail is larger than 4KB it uses Piped streams and a worker threads
 * Otherwise it simply create a temporary byte buffer and does not create
 * the worker thread.
 * 
 * Note: Javamail (or the Activation Framework) already uses a worker threads when
 * asked for an inputstream.
 */
final class MessageInputStream extends InputStream {
    
    /**
     * The size of the current message
     */
    private long size = -1;
    /**
     * The wrapped stream (Piped or Binary)
     */
    private InputStream wrapped;
    /**
     * If an excaption happens in the worker threads it's stored here
     */
    private Exception caughtException;
    /**
     * Stream repository used for dbfiles (null otherwise)
     */
    private StreamRepository streamRep;
    
    /**
     * Main constructor. If srep is not null than we are using dbfiles and we stream
     * the body to file and only the header to db.
     */
    public MessageInputStream(Mail mc, StreamRepository srep, int sizeLimit) throws IOException, MessagingException {
        super();
        caughtException = null;
        streamRep = srep;
        size = mc.getMessageSize();
        // we use the pipes only when streamRep is null and the message size is greater than 4096
        // Otherwise we should calculate the header size and not the message size when streamRep is not null (JAMES-475)
        if (streamRep == null && size > sizeLimit) {
            PipedOutputStream headerOut = new PipedOutputStream();
            new Thread() {
                private Mail mail;

                private PipedOutputStream out;

                public void run() {
                    try {
                        writeStream(mail,out);
                    } catch (IOException e) {
                        caughtException = e;
                    } catch (MessagingException e) {
                        caughtException = e;
                    }
                }

                public Thread setParam(Mail mc, PipedOutputStream headerOut) {
                    this.mail = mc;
                    this.out = headerOut;
                    return this;
                }
            }.setParam(mc,(PipedOutputStream) headerOut).start();
            wrapped = new PipedInputStream(headerOut);
        } else {
            ByteArrayOutputStream headerOut = new ByteArrayOutputStream();
            writeStream(mc,headerOut);
            wrapped = new ByteArrayInputStream(headerOut.toByteArray());
            size = headerOut.size();
        }
    }
    
    /**
     * Returns the size of the full message
     */
    public long getSize() {
        return size;
    }

    /**
     * write the full mail to the stream
     * This can be used by this object or by the worker threads.
     */
    private void writeStream(Mail mail, OutputStream out) throws IOException, MessagingException {
        OutputStream bodyOut = null;
        try {
            if (streamRep == null) {
                //If there is no filestore, use the byte array to store headers
                //  and the body
                bodyOut = out;
            } else {
                //Store the body in the stream repository
                bodyOut = streamRep.put(mail.getName());
            }
        
            //Write the message to the headerOut and bodyOut.  bodyOut goes straight to the file
            MimeMessageUtil.writeTo(mail.getMessage(), out, bodyOut);
            out.flush();
            bodyOut.flush();
        
        } finally {
            closeOutputStreams(out, bodyOut);
        }
    }

    private void throwException() throws IOException {
        try {
            if (wrapped == null) {
                throw new IOException("wrapped stream does not exists anymore");
            } else if (caughtException instanceof IOException) {
                throw (IOException) caughtException;
            } else {
                throw new IOException("Exception caugth in worker thread "+caughtException.getMessage()) {
                    /**
                     * @see java.lang.Throwable#getCause()
                     */
                    public Throwable getCause() {
                        return caughtException;
                    }
                };
            }
        } finally {
            caughtException = null;
            wrapped = null;
        }
    }


    /**
     * Closes output streams used to update message
     * 
     * @param headerStream the stream containing header information - potentially the same
     *               as the body stream
     * @param bodyStream the stream containing body information
     * @throws IOException 
     */
    private void closeOutputStreams(OutputStream headerStream, OutputStream bodyStream) throws IOException {
        try {
            // If the header stream is not the same as the body stream,
            // close the header stream here.
            if ((headerStream != null) && (headerStream != bodyStream)) {
                headerStream.close();
            }
        } finally {
            if (bodyStream != null) {
                bodyStream.close();
            }
        }
    }

    // wrapper methods

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        if (caughtException != null || wrapped == null) {
            throwException();
        }
        return wrapped.available();
    }

    /**
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException {
        if (caughtException != null || wrapped == null) {
            throwException();
        }
        wrapped.close();
        wrapped = null;
    }

    /**
     * @see java.io.InputStream#mark(int)
     */
    public synchronized void mark(int arg0) {
        wrapped.mark(arg0);
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return wrapped.markSupported();
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        if (caughtException != null || wrapped == null) {
            throwException();
        }
        return wrapped.read(arg0, arg1, arg2);
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] arg0) throws IOException {
        if (caughtException != null || wrapped == null) {
            throwException();
        }
        return wrapped.read(arg0);
    }

    /**
     * @see java.io.InputStream#reset()
     */
    public synchronized void reset() throws IOException {
        if (caughtException != null || wrapped == null) {
            throwException();
        }
        wrapped.reset();
    }

    /**
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws IOException {
        if (caughtException != null || wrapped == null) {
            throwException();
        }
        return wrapped.skip(arg0);
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (caughtException != null || wrapped == null) {
            throwException();
        }
        return wrapped.read();
    }

}
