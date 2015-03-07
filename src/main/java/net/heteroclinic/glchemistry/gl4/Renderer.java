package net.heteroclinic.glchemistry.gl4;

import javax.media.opengl.GLEventListener;

abstract public class Renderer implements GLEventListener{
	protected IInstancedRenderingView view;
	
	public IInstancedRenderingView getView() {
		return view;
	}

	public void setView(IInstancedRenderingView view) {
		this.view = view;
	}
}
