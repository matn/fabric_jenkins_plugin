package org.fabfile.jenkins.plugins;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ArgumentListBuilder;
import hudson.EnvVars;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder_} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class FabricScriptBuilder extends Builder {

    private final String name;
    private final String fabfile;
    private final String command;
    private final String user;
    private final String host;
    private final String role;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public FabricScriptBuilder(String name, String fabfile, String command, String user, String host, String role) {
        this.name = name;
        this.fabfile = fabfile;
        this.command = command;
        this.user = user;
        this.host = host;
        this.role = role;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getFabfile() {
        return fabfile;
    }
    public String getCommand() {
        return command;
    }
    public String getUser() {
        return user;
    }
    public String getHost() {
        return host;
    }
    public String getRole() {
        return role;
    }
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	
    	ArgumentListBuilder args = new ArgumentListBuilder();        
    	String fabricExecutable = getDescriptor().getFabricExecutable();
    	
    	if (fabricExecutable != null && fabricExecutable.trim().length() != 0){
    		args.add(fabricExecutable);
    	}else{
    		listener.getLogger().println(Messages.Please_Configure_Fabric());
    		return false;
    	}
    	
    	if (fabfile != null && fabfile.trim().length() != 0){
    		args.add("--fabfile=" + fabfile);
    	}
    	
    	if (user != null && user.trim().length() != 0){
    		args.add("--user="+user);
    	}
    	
    	if (host != null && host.trim().length() != 0){
    		args.add("--host="+host);
    	}
    	
    	if (role != null && role.trim().length() != 0){
    		args.add("--roles="+role);
    	}
    	
    	if (command != null && command.trim().length() != 0){
    		args.add(command);
    	}else{
    		listener.getLogger().println(Messages.No_Command_Specified());
    		return false;	
    	}

        
        try {
        	EnvVars env = build.getEnvironment(listener);	
        	launcher.launch().cmds(args).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getModuleRoot()).join();
        } catch (IOException e) {
        	//Util.displayIOException(e, listener);
        	//e.printStackTrace(listener.fatalError(Messages.scons_commandExecutionFailed()));
            return false;
        } catch (InterruptedException e){
        	return false;
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link HelloWorldBuilder_}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        
        private String fabricExecutable;
        
        public DescriptorImpl() {
            load();
        }
        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckCommand(@QueryParameter String value)
        		throws IOException, ServletException {
        	if (value.length() == 0)
        		return FormValidation.error(Messages.Please_Set_Command());
        	return FormValidation.ok();
        }
        public FormValidation doCheckFabfile(@QueryParameter String value)
				throws IOException, ServletException {
        	if (value.length() == 0)
        		return FormValidation.error(Messages.Please_Set_Fabfile());
        	return FormValidation.ok();
}
        

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
        	return Messages.Invoke_Fabric_Script();
            //return "Invoke Fabric script (fabfile)";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            //useFrench = formData.getBoolean("useFrench");
            fabricExecutable = formData.getString("fabricExecutable");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         */
        /*public boolean useFrench() {
            return useFrench;
        }*/
        public String getFabricExecutable(){
        	return fabricExecutable;
        }
    }
}

