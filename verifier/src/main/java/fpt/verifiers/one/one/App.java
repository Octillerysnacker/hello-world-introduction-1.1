package fpt.verifiers.one.one;

import java.nio.file.Paths;
import com.google.gson.Gson;
import fpt.verifiers.core.IVerifier;
import fpt.verifiers.core.VerifierResult;
import fpt.verifiers.core.FPTDiagnostic;
/**
 * Hello world!
 *
 */
public class App 
{
    private static Gson gson = new Gson();
    public static void main( String[] args )
    {

        IVerifier verifier = new Verifier();
        
        try{
            var userDirectory = Paths.get(args[0]);
            var projectDirectory = Paths.get(args[1]);
            System.out.println(gson.toJson(verifier.Verify(userDirectory,projectDirectory)));
        }catch(Exception e){
            System.out.println(
                gson.toJson(
                    new VerifierResult(
                        false, 
                        new FPTDiagnostic[]{new FPTDiagnostic(e.toString(),FPTDiagnostic.DiagnosticKind.Error)})));
        }
    }
}
