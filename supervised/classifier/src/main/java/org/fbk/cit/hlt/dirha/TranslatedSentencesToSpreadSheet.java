package org.fbk.cit.hlt.dirha;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.fbk.cit.hlt.core.analysis.tokenizer.HardTokenizer;
import org.fbk.cit.hlt.core.analysis.tokenizer.Tokenizer;

import java.io.*;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 10/4/13
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This class takes as input a file with a (translated) sentence per line and returns
 * a file with the sentences tokenized/pos tagged/lemmatized that can be
 * annotated with the guidelines described at https://docs.google.com/spreadsheet/ccc?key=0AoGh3TWKyYNPdHN3bmNTVmZuRGo5M0lYZlRzNDlVZHc&usp=drive_web#gid=6
 * using the Web interface designed by Alessio Palmero Aprosio or any spreadsheet.
 *
 * @see AnnotationMigration
 */
public class TranslatedSentencesToSpreadSheet {
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>TranslatedSentencesToSpreadSheet</code>.
	 */
	static Logger logger = Logger.getLogger(TranslatedSentencesToSpreadSheet.class.getName());

	public TranslatedSentencesToSpreadSheet(File fin, File fout, int start, int size, String lang) throws Exception {
		Tokenizer tokenier = new HardTokenizer();
		// Point TT4J to the TreeTagger installation directory. The executable is expected
		// in the "bin" subdirectory - in this example at "/opt/treetagger/bin/tree-tagger"
		System.setProperty("treetagger.home", "/Users/giuliano/Applications/treetagger");
		TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();

		LineNumberReader lr = new LineNumberReader(new FileReader(fin));
		PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout), "UTF-8")));
		String line = null;

		if (lang.equalsIgnoreCase("en")) {
			tt.setModel("/Users/giuliano/Applications/treetagger/lib/english.par");
		}
		else if (lang.equalsIgnoreCase("de")) {
			tt.setModel("/Users/giuliano/Applications/treetagger/lib/german.par");
		} else {
			logger.error(lang + " is not supported");
		}

		int count = 0;
		int end = start + size;
		logger.info("extracting from " + start + " to " + end);
		while ((line = lr.readLine()) != null) {
			if (count >= start && count < end) {
				try {

					pw.println("sid\ttid\ttoken\tpos\tlemma\tframe\tLU/FE\tnotes\tstart/end time");
					String[] s = line.split("\t");
					if (s.length == 2) {
						int id = Integer.parseInt(s[0]);
						MyTokenHandler myTokenHandler = new MyTokenHandler<String>(pw, id);
						tt.setHandler(myTokenHandler);

						String[] tokens = tokenier.stringArray(s[1]);
						//logger.debug(Arrays.toString(tokens));
						tt.process(tokens);
						pw.print("\n");

					}


				} catch (Exception e) {
					logger.error(e);
				}
			}
			count++;
		}
		pw.close();
		tt.destroy();
	}


	class MyTokenHandler<O> implements TokenHandler<O> {

		private PrintWriter pw;
		private int count;
		private int tc;

		MyTokenHandler(PrintWriter pw, int count) {
			this.pw = pw;
			this.count = count;
			tc = 0;
		}

		@Override
		public void token(O o, String pos, String lemma) {
			//To change body of implemented methods use File | Settings | File Templates.
			pw.println(count + "\t" + (++tc) + "\t" + replaceQuote((String) o) + "\t" + pos + "\t" + replaceQuote(lemma) + "\t\tO");
		}

		private String replaceQuote(String s) {
			if (s.equals("'")) {
				return "′";
			}
			return s;
		}
	}

	public static void main(String[] args) throws Exception {
		//java -cp dist/dirha.jar org.fbk.cit.hlt.dirha.TranslatedSentencesToSpreadSheet
		String logConfig = System.getProperty("log-config");
		if (logConfig == null) {
			logConfig = "log-config.txt";
		}
		PropertyConfigurator.configure(logConfig);

		if (args.length != 3) {
			logger.error("java -Dfile.encoding=UTF-8 -cp dist/dirha.jar org.fbk.cit.hlt.dirha.TranslatedSentencesToSpreadSheet fin fout lang");
			System.exit(-1);
		}
		File fin = new File(args[0]);
		File fout = new File(args[1]);

		//int start = Integer.parseInt(args[2]);
		//int size = Integer.parseInt(args[3]);
		new TranslatedSentencesToSpreadSheet(fin, fout, 0, Integer.MAX_VALUE, args[2]);
	}
}